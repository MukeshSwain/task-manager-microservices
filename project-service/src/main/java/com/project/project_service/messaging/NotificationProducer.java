package com.project.project_service.messaging;

import com.project.project_service.config.RabbitConfig;
import com.project.project_service.dto.EmailRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    public void sendEvent(EmailRequest event,String routingKey){
        rabbitTemplate.convertAndSend(RabbitConfig.PROJECT_EXCHANGE, routingKey, event);
    }

}
