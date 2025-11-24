package com.notification.notification_service.messaging;

import com.notification.notification_service.config.RabbitConfig;
import com.notification.notification_service.dto.EmailEvent;
import com.notification.notification_service.dto.UserInvitedEvent;
import com.notification.notification_service.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InvitationEmailListener {
    private final EmailService emailService;

    public InvitationEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.INVITE_QUEUE)
    public void onUserInvited(UserInvitedEvent event){
        log.info("User invited event received: {}", event);
        emailService.sendInvitationEmail(event);
        log.info("📤 Invitation email sent to {}", event.getEmail());
    }

    @RabbitListener(queues = RabbitConfig.MEMBER_REMOVED_QUEUE)
    public void onMemberRemoved(EmailEvent event){
        log.info("Member removed event received: {}", event);
        emailService.sendMemberRemovedEmail(event);
        log.info("📤 Member removed email sent to {}", event.getEmail());
    }

    @RabbitListener(queues = RabbitConfig.ROLE_UPDATED_QUEUE)
    public void onRoleUpdated(EmailEvent event){
        log.info("Role updated event received: {}", event);
        emailService.sendRoleUpdatedEmail(event);
        log.info("📤 Role updated email sent to {}", event.getEmail());
    }

    @RabbitListener(queues = RabbitConfig.MEMBER_ADDED_QUEUE)
    public void onMemberAdded(EmailEvent event){
        log.info("Member added event received: {}", event);
        emailService.sendMemberAdded(event);
        log.info("📤 Member added email sent to {}", event.getEmail());
    }
}
