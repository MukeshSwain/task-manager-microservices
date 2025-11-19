package com.notification.notification_service.messaging;

import com.notification.notification_service.config.RabbitConfig;
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

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onUserInvited(UserInvitedEvent event){
        log.info("User invited event received: {}", event);
        emailService.sendInvitationEmail(event);
        log.info("📤 Invitation email sent to {}", event.getEmail());
    }
}
