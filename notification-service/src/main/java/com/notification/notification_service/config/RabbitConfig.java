package com.notification.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;


import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    // --- Inject Values from Properties ---
    @Value("${app.rabbitmq.exchange.events}")
    private String eventExchangeName;

    @Value("${app.rabbitmq.exchange.project}")
    private String projectExchangeName;

    @Value("${app.rabbitmq.queue.project-created}")
    private String projectCreatedQueue;

    @Value("${app.rabbitmq.queue.project-member-added}")
    private String projectMemberAddedQueue;

    // --- Constants ---
    // Routing Keys (These stay in code as they are logical paths)
    public static final String PROJECT_CREATED_KEY = "project.created";
    public static final String PROJECT_MEMBER_ADDED_KEY = "project.member.added";

    // DLQ Constants
    public static final String DLQ_EXCHANGE = "dead-letter.exchange";
    public static final String DLQ_SUFFIX = ".dlq";

    // ----------------------------------------------------------------
    // 1. EXCHANGES
    // ----------------------------------------------------------------

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(eventExchangeName);
    }

    @Bean
    public TopicExchange projectExchange() {
        return new TopicExchange(projectExchangeName);
    }

    // ✅ SENIOR: The "Graveyard" Exchange for failed messages
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLQ_EXCHANGE);
    }

    // ----------------------------------------------------------------
    // 2. QUEUES (With DLQ Configuration)
    // ----------------------------------------------------------------

    // Helper method to create a queue with DLQ attached
    private Queue createQueueWithDlq(String queueName, String routingKey) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", routingKey)
                .build();
    }

    // Helper method to create the actual DLQ queue
    private Queue createDlq(String queueName) {
        return QueueBuilder.durable(queueName + DLQ_SUFFIX).build();
    }

    // -- Project Created Queue & DLQ --
    @Bean
    public Queue projectQueue() {
        return createQueueWithDlq(projectCreatedQueue, PROJECT_CREATED_KEY);
    }
    @Bean
    public Queue projectQueueDlq() {
        return createDlq(projectCreatedQueue);
    }

    // -- Project Member Added Queue & DLQ --
    @Bean
    public Queue projectMemberAddedQueue() {
        return createQueueWithDlq(projectMemberAddedQueue, PROJECT_MEMBER_ADDED_KEY);
    }
    @Bean
    public Queue projectMemberAddedQueueDlq() {
        return createDlq(projectMemberAddedQueue);
    }

    // ----------------------------------------------------------------
    // 3. BINDINGS
    // ----------------------------------------------------------------

    // Bind Main Queues
    @Bean
    public Binding projectBinding() {
        return BindingBuilder.bind(projectQueue())
                .to(projectExchange())
                .with(PROJECT_CREATED_KEY);
    }

    @Bean
    public Binding projectMemberAddedBinding() {
        return BindingBuilder.bind(projectMemberAddedQueue())
                .to(projectExchange())
                .with(PROJECT_MEMBER_ADDED_KEY);
    }

    // ✅ SENIOR: Bind the DLQs so we can actually see the dead messages
    @Bean
    public Binding projectDlqBinding() {
        return BindingBuilder.bind(projectQueueDlq())
                .to(deadLetterExchange())
                .with(PROJECT_CREATED_KEY);
    }

    @Bean
    public Binding projectMemberAddedDlqBinding() {
        return BindingBuilder.bind(projectMemberAddedQueueDlq())
                .to(deadLetterExchange())
                .with(PROJECT_MEMBER_ADDED_KEY);
    }

    // ----------------------------------------------------------------
    // 4. INFRASTRUCTURE & RETRY STRATEGY
    // ----------------------------------------------------------------

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // ✅ SENIOR: Listener Factory with Retry Logic and Concurrency
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());

        // Performance: Process 3 messages in parallel
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);

        // Resilience: Add Retry Interceptor
        factory.setAdviceChain(retryInterceptor());

        return factory;
    }

    // ✅ SENIOR: Exponential Backoff Retry Policy
    // Attempt 1 -> Fail -> Wait 1s -> Attempt 2 -> Fail -> Wait 2s -> Attempt 3 -> Fail -> DLQ
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return org.springframework.amqp.rabbit.config.RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000) // Initial 1s, Multiplier 2x, Max 10s
                .build();
    }
}