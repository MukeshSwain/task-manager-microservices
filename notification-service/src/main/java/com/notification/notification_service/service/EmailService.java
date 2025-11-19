package com.notification.notification_service.service;

import com.notification.notification_service.dto.UserInvitedEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendInvitationEmail(UserInvitedEvent event) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(event.getEmail());
        msg.setSubject("You're invited to join our platform!");
        msg.setText(
                "Hello,\n\n" +
                        "You have been invited to join an organization on our platform.\n" +
                        "Organization ID: " + event.getOrgId() + "\n" +
                        "Role: " + event.getRole() + "\n\n" +
                        "Click the link below to accept the invitation:\n" +
                        "https://yourapp.com/signup?token=" + event.getInviteToken() + "\n\n" +
                        "Regards,\nYour SaaS Team"
        );

        javaMailSender.send(msg);

    }
}
