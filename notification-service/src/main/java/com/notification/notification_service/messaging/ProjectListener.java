package com.notification.notification_service.messaging;

import com.notification.notification_service.config.RabbitConfig;
import com.notification.notification_service.dto.EmailRequest;
import com.notification.notification_service.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class ProjectListener {
    private final EmailService emailService;

    public ProjectListener(EmailService emailService) {
        this.emailService = emailService;
    }
    @RabbitListener(queues = RabbitConfig.PROJECT_CREATED_KEY)
    public void handleProjectCreatedEvent(EmailRequest event) {
        emailService.sendProjectCreatedEmail(event);
    }
}
