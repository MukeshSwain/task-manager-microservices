package com.notification.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "events.exchange";

    // Routing Keys
    public static final String INVITE_KEY = "email.invite";
    public static final String MEMBER_ADDED_KEY = "email.member.added";
    public static final String ROLE_UPDATED_KEY = "email.member.role.updated";
    public static final String MEMBER_REMOVED_KEY = "email.member.removed";

    // Queues
    public static final String INVITE_QUEUE = "email.invite.queue";
    public static final String MEMBER_ADDED_QUEUE = "email.member.added.queue";
    public static final String ROLE_UPDATED_QUEUE = "email.member.role.updated.queue";
    public static final String MEMBER_REMOVED_QUEUE = "email.member.removed.queue";

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue inviteQueue() { return QueueBuilder.durable(INVITE_QUEUE).build(); }

    @Bean
    public Queue memberAddedQueue() { return QueueBuilder.durable(MEMBER_ADDED_QUEUE).build(); }

    @Bean
    public Queue roleUpdatedQueue() { return QueueBuilder.durable(ROLE_UPDATED_QUEUE).build(); }

    @Bean
    public Queue memberRemovedQueue() { return QueueBuilder.durable(MEMBER_REMOVED_QUEUE).build(); }

    // Bindings
    @Bean
    public Binding inviteBinding(Queue inviteQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(inviteQueue).to(eventExchange).with(INVITE_KEY);
    }

    @Bean
    public Binding memberAddedBinding(Queue memberAddedQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(memberAddedQueue).to(eventExchange).with(MEMBER_ADDED_KEY);
    }

    @Bean
    public Binding roleUpdatedBinding(Queue roleUpdatedQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(roleUpdatedQueue).to(eventExchange).with(ROLE_UPDATED_KEY);
    }

    @Bean
    public Binding memberRemovedBinding(Queue memberRemovedQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(memberRemovedQueue).to(eventExchange).with(MEMBER_REMOVED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
