package com.notification.notification_service.messaging;

import com.notification.notification_service.config.RabbitConfig;
import com.notification.notification_service.dto.EmailRequest;
import com.notification.notification_service.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProjectListener {
    private final EmailService emailService;

    public ProjectListener(EmailService emailService) {
        this.emailService = emailService;
    }
    @RabbitListener(queues = "${app.rabbitmq.queue.project-created}")
    public void handleProjectCreatedEvent(EmailRequest event) {
        emailService.sendProjectCreatedEmail(event);
    }
    @RabbitListener(queues = "${app.rabbitmq.queue.project-member-added}")
    public void handleProjectMemberAddedEvent(EmailRequest event) {
        log.info("1. Listener triggered!");
        log.info("2. Payload received for: {}", event.getToEmail());
        emailService.sendProjectMemberAddedEmail(event);
        log.info("3. Email sent successfully to: {}", event.getToEmail());
    }
    @RabbitListener(queues = "${app.rabbitmq.queue.new-lead-assigned}")
    public void handleNewLeadAssignedEvent(EmailRequest event){
        emailService.sendNewLeadAssigned(event);
    }
}
