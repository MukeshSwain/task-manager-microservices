package com.notification.notification_service.messaging;

import com.notification.notification_service.dto.EmailEvent;
import com.notification.notification_service.dto.EmailRequest;
import com.notification.notification_service.dto.UserInvitedEvent;
import com.notification.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor // Clean way to generate constructor for final fields
// 1. Rename to reflect that it handles ALL notifications, not just invites
public class InvitationEmailListener {

    private final EmailService emailService;

    // 2. Use Property Placeholders ("${...}")
    // This allows you to change queue names in application.properties without recompiling code.
    // It matches the config we created in the previous step.

    @RabbitListener(queues = "${app.rabbitmq.queue.invite}")
    public void onUserInvited(UserInvitedEvent event) {
        log.info("📧 Received Invite Event for: {}", event.getEmail());
        emailService.sendInvitationEmail(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.member-removed}")
    public void onMemberRemoved(EmailEvent event) {
        log.info("📧 Received Member Removed Event for: {}", event.getEmail());
        emailService.sendMemberRemovedEmail(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.role-updated}")
    public void onRoleUpdated(EmailEvent event) {
        log.info("📧 Received Role Update Event for: {}", event.getEmail());
        emailService.sendRoleUpdatedEmail(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.member-added}")
    public void onMemberAdded(EmailEvent event) {
        log.info("📧 Received Member Added Event for: {}", event.getEmail());
        emailService.sendMemberAdded(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.project-created}")
    public void onProjectCreated(EmailRequest event) {
        log.info("📧 Received Project Created Event for: {}", event.getToEmail());
        emailService.sendProjectCreatedEmail(event);
    }

    // Note: We REMOVED the "log.info('sent successfully')" after the service call.
    // Why? Because if the service fails, it throws an exception, and the log never happens.
    // The Logging inside the Service (or an AOP Aspect) is a better place for "Success" logs.
}