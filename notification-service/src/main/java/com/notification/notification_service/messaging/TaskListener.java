package com.notification.notification_service.messaging;

import com.notification.notification_service.dto.TaskAssignedEvent;
import com.notification.notification_service.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskListener {
    private final EmailService emailService;
    public TaskListener(EmailService emailService) {
        this.emailService = emailService;
    }
    @RabbitListener(queues = "${app.rabbitmq.queue.task-assigned}")
    public void listen(TaskAssignedEvent event){
        log.info("Task assigned event received: {}", event);
        emailService.sendTaskAssignedEmail(event);
    }
}
