package com.tenant.tenant_service.messaging;

import com.tenant.tenant_service.config.RabbitConfig;
import com.tenant.tenant_service.dto.EmailEvent;
import com.tenant.tenant_service.dto.UserInvitedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendUserInvitedEvent(UserInvitedEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.INVITE_KEY, event);
    }

    public void send(EmailEvent event, String routingKey){
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey,event);

    }

}
