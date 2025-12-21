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

@Configuration
public class RabbitConfig {

    // ----------------------------------------------------------------
    // 0. PROPERTIES & CONSTANTS
    // ----------------------------------------------------------------

    // --- Exchange Names ---
    @Value("${app.rabbitmq.exchange.events}")
    private String eventExchangeName;

    @Value("${app.rabbitmq.exchange.project}")
    private String projectExchangeName;

    // --- Queue Names (Project) ---
    @Value("${app.rabbitmq.queue.project-created}")
    private String projectCreatedQueue;

    @Value("${app.rabbitmq.queue.project-member-added}")
    private String projectMemberAddedQueue;

    // --- Queue Names (Email/Notifications) - ✅ ADDED THESE
    @Value("${app.rabbitmq.queue.invite}")
    private String inviteQueue;

    @Value("${app.rabbitmq.queue.member-added}")
    private String memberAddedQueue;

    @Value("${app.rabbitmq.queue.member-removed}")
    private String memberRemovedQueue;

    @Value("${app.rabbitmq.queue.role-updated}")
    private String roleUpdatedQueue;

    @Value("${app.rabbitmq.queue.new-lead-assigned}")
    private String newLeadAssignedQueue;

    // --- Routing Keys ---
    public static final String PROJECT_CREATED_KEY = "project.created";
    public static final String PROJECT_MEMBER_ADDED_KEY = "project.member.added";

    // ✅ ADDED MISSING KEYS
    public static final String INVITE_KEY = "email.invite";
    public static final String MEMBER_ADDED_KEY = "email.member.added";
    public static final String MEMBER_REMOVED_KEY = "email.member.removed";
    public static final String ROLE_UPDATED_KEY = "email.member.role.updated";
    public static final String NEW_LEAD_ASSIGNED_KEY = "project.new.lead.assigned";

    // --- DLQ Constants ---
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

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLQ_EXCHANGE);
    }

    // ----------------------------------------------------------------
    // 2. QUEUE FACTORY METHODS
    // ----------------------------------------------------------------

    // Helper: Creates a durable queue with DLQ arguments
    private Queue createQueueWithDlq(String queueName, String routingKey) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", routingKey)
                .build();
    }

    // Helper: Creates the actual Dead Letter Queue
    private Queue createDlq(String queueName) {
        return QueueBuilder.durable(queueName + DLQ_SUFFIX).build();
    }

    // ----------------------------------------------------------------
    // 3. QUEUE DEFINITIONS
    // ----------------------------------------------------------------

    // --- PROJECT QUEUES ---
    @Bean public Queue projectQueue() { return createQueueWithDlq(projectCreatedQueue, PROJECT_CREATED_KEY); }
    @Bean public Queue projectQueueDlq() { return createDlq(projectCreatedQueue); }

    @Bean public Queue projectNewLeadAssignedQueue() { return createQueueWithDlq(newLeadAssignedQueue, NEW_LEAD_ASSIGNED_KEY); }
    @Bean public Queue projectNewLeadAssignedQueueDlq() { return createDlq(newLeadAssignedQueue); }

    @Bean public Queue projectMemberAddedQueue() { return createQueueWithDlq(projectMemberAddedQueue, PROJECT_MEMBER_ADDED_KEY); }
    @Bean public Queue projectMemberAddedQueueDlq() { return createDlq(projectMemberAddedQueue); }

    // --- ✅ ADDED: EMAIL NOTIFICATION QUEUES ---
    @Bean public Queue inviteQueue() { return createQueueWithDlq(inviteQueue, INVITE_KEY); }
    @Bean public Queue inviteQueueDlq() { return createDlq(inviteQueue); }

    @Bean public Queue memberAddedQueue() { return createQueueWithDlq(memberAddedQueue, MEMBER_ADDED_KEY); }
    @Bean public Queue memberAddedQueueDlq() { return createDlq(memberAddedQueue); }

    @Bean public Queue memberRemovedQueue() { return createQueueWithDlq(memberRemovedQueue, MEMBER_REMOVED_KEY); }
    @Bean public Queue memberRemovedQueueDlq() { return createDlq(memberRemovedQueue); }

    @Bean public Queue roleUpdatedQueue() { return createQueueWithDlq(roleUpdatedQueue, ROLE_UPDATED_KEY); }
    @Bean public Queue roleUpdatedQueueDlq() { return createDlq(roleUpdatedQueue); }

    // ----------------------------------------------------------------
    // 4. BINDINGS
    // ----------------------------------------------------------------

    // --- PROJECT BINDINGS ---
    @Bean
    public Binding projectLeadAssignedBinding() {
        return BindingBuilder.bind(projectNewLeadAssignedQueue()).to(projectExchange()).with(NEW_LEAD_ASSIGNED_KEY);
    }
    @Bean
    public Binding projectLeadAssignedDlqBinding() {
        return BindingBuilder.bind(projectNewLeadAssignedQueueDlq()).to(deadLetterExchange()).with(NEW_LEAD_ASSIGNED_KEY);
    }
    @Bean
    public Binding projectBinding() {
        return BindingBuilder.bind(projectQueue()).to(projectExchange()).with(PROJECT_CREATED_KEY);
    }
    @Bean
    public Binding projectDlqBinding() {
        return BindingBuilder.bind(projectQueueDlq()).to(deadLetterExchange()).with(PROJECT_CREATED_KEY);
    }

    @Bean
    public Binding projectMemberAddedBinding() {
        return BindingBuilder.bind(projectMemberAddedQueue()).to(projectExchange()).with(PROJECT_MEMBER_ADDED_KEY);
    }
    @Bean
    public Binding projectMemberAddedDlqBinding() {
        return BindingBuilder.bind(projectMemberAddedQueueDlq()).to(deadLetterExchange()).with(PROJECT_MEMBER_ADDED_KEY);
    }

    // --- ✅ ADDED: EMAIL BINDINGS (Bound to eventExchange) ---
    @Bean
    public Binding inviteBinding() {
        return BindingBuilder.bind(inviteQueue()).to(eventExchange()).with(INVITE_KEY);
    }
    @Bean
    public Binding inviteDlqBinding() {
        return BindingBuilder.bind(inviteQueueDlq()).to(deadLetterExchange()).with(INVITE_KEY);
    }

    @Bean
    public Binding memberAddedBinding() {
        return BindingBuilder.bind(memberAddedQueue()).to(eventExchange()).with(MEMBER_ADDED_KEY);
    }
    @Bean
    public Binding memberAddedDlqBinding() {
        return BindingBuilder.bind(memberAddedQueueDlq()).to(deadLetterExchange()).with(MEMBER_ADDED_KEY);
    }

    @Bean
    public Binding memberRemovedBinding() {
        return BindingBuilder.bind(memberRemovedQueue()).to(eventExchange()).with(MEMBER_REMOVED_KEY);
    }
    @Bean
    public Binding memberRemovedDlqBinding() {
        return BindingBuilder.bind(memberRemovedQueueDlq()).to(deadLetterExchange()).with(MEMBER_REMOVED_KEY);
    }

    @Bean
    public Binding roleUpdatedBinding() {
        return BindingBuilder.bind(roleUpdatedQueue()).to(eventExchange()).with(ROLE_UPDATED_KEY);
    }
    @Bean
    public Binding roleUpdatedDlqBinding() {
        return BindingBuilder.bind(roleUpdatedQueueDlq()).to(deadLetterExchange()).with(ROLE_UPDATED_KEY);
    }

    // ----------------------------------------------------------------
    // 5. INFRASTRUCTURE & RETRY STRATEGY
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

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return org.springframework.amqp.rabbit.config.RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .build();
    }
}