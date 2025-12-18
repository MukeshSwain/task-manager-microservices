package com.notification.notification_service.service;

import com.notification.notification_service.dto.EmailEvent;
import com.notification.notification_service.dto.EmailRequest;
import com.notification.notification_service.dto.UserInvitedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
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
                        "http://localhost:5173/signup/invite?token=" + event.getInviteToken() + "\n\n" +
                        "Regards,\nYour SaaS Team"
        );

        javaMailSender.send(msg);

    }

    public void sendMemberRemovedEmail(EmailEvent event) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(event.getEmail());
        msg.setSubject(event.getSubject());
        msg.setText(event.getMessage());
        javaMailSender.send(msg);
    }

    public void sendRoleUpdatedEmail(EmailEvent event) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(event.getEmail());
        msg.setSubject(event.getSubject());
        msg.setText(event.getMessage());
        javaMailSender.send(msg);
    }

    public void sendMemberAdded(EmailEvent event){
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(event.getEmail());
        msg.setSubject(event.getSubject());
        msg.setText(event.getMessage());
        javaMailSender.send(msg);
    }
    public void sendProjectCreatedEmail(EmailRequest event) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();

            // 1. Set Basic Info
            msg.setTo(event.getToEmail());
            msg.setSubject(event.getSubject());
            msg.setFrom("no-reply@yourcompany.com"); // Good practice to set a sender

            // 2. Construct the Message Body from Variables
            String body = buildPlainTextBody(event.getVariables());
            msg.setText(body);

            // 3. Send
            javaMailSender.send(msg);
            log.info("Email sent successfully to {}", event.getToEmail());

        } catch (Exception e) {
            log.error("Failed to send email to {}", event.getToEmail(), e);
            // Optional: Throw exception if you want RabbitMQ to retry
        }
    }

    private String buildPlainTextBody(Map<String, Object> vars) {
        if (vars == null) return "A new project has been created.";

        // Use the specific keys we put in the ProjectService earlier
        String projectName = (String) vars.getOrDefault("projectName", "Unknown Project");
        String ownerName = (String) vars.getOrDefault("ownerName", "User");
        String link = (String) vars.getOrDefault("dashboardLink", "#");

        return String.format("""
            Hello %s,
            
            A new project "%s" has been successfully created in your workspace.
            
            You can view the project details here:
            %s
            
            Best regards,
            The Project Management Team
            """, ownerName, projectName, link);
    }

}
