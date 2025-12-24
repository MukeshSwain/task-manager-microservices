package com.task.task_service.messaging;

import com.task.task_service.dto.TaskAssignedEvent;
import com.task.task_service.messaging.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;
    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTaskAssignedEvent(TaskAssignedEvent event,String routingKey){
        log.info("Publishing task assigned event: {}", event);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,routingKey,event);
        log.info("Task assigned event published successfully");
    }
}
