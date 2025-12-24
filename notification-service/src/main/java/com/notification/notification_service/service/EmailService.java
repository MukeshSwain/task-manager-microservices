package com.notification.notification_service.service;

import com.notification.notification_service.dto.EmailEvent;
import com.notification.notification_service.dto.EmailRequest;
import com.notification.notification_service.dto.TaskAssignedEvent;
import com.notification.notification_service.dto.UserInvitedEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
            log.info("1. Listener triggered!");
            log.info("2. Payload received for: {}", event.getToEmail());
            // Load HTML template
            String htmlTemplate = loadEmailTemplate(event.getTemplateCode());

            // Replace variables {{key}}
            String processedHtml = replaceVariables(
                    htmlTemplate,
                    event.getVariables()
            );

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(event.getToEmail());
            helper.setSubject(event.getSubject());
            helper.setText(processedHtml, true); // true = HTML

            javaMailSender.send(message);


        } catch (Exception e) {
            log.error("Failed to send email to {}", event.getToEmail(), e);
            // Optional: Throw exception if you want RabbitMQ to retry
        }
    }
    public void sendNewLeadAssigned(EmailRequest event){

        try{
            log.info("1. Listener triggered!");
            log.info("2. Payload received for: {}", event.getToEmail());
            String htmlTemplate = loadEmailTemplate(event.getTemplateCode());
            String processedHtml = replaceVariables(
                    htmlTemplate,
                    event.getVariables()
            );
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true, "UTF-8");
            helper.setTo(event.getToEmail());
            helper.setSubject(event.getSubject());
            helper.setText(processedHtml, true);

            javaMailSender.send(message);
            log.info("New lead assigned email has been sent!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void sendProjectMemberAddedEmail(EmailRequest event) {
        try {
            log.info("1. Listener triggered!");
            log.info("2. Payload received for: {}", event.getToEmail());
            // Load HTML template
            String htmlTemplate = loadEmailTemplate(event.getTemplateCode());

            // Replace variables {{key}}
            String processedHtml = replaceVariables(
                    htmlTemplate,
                    event.getVariables()
            );

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(event.getToEmail());
            helper.setSubject(event.getSubject());
            helper.setText(processedHtml, true); // true = HTML

            javaMailSender.send(message);

            log.info("HTML project member added email sent to {}", event.getToEmail());

        } catch (Exception e) {
            log.error("Failed to send email to {}", event.getToEmail(), e);
        }
    }
    private String loadEmailTemplate(String templateCode) throws IOException {
        if (!templateCode.endsWith(".html")) {
            templateCode += ".html";
        }

        ClassPathResource resource = new ClassPathResource("templates/" + templateCode);
        // 3. Check if file exists (Optional but good for debugging)
        if (!resource.exists()) {
            throw new FileNotFoundException("Template file not found: templates/" + templateCode);
        }

        return StreamUtils.copyToString(
                resource.getInputStream(),
                StandardCharsets.UTF_8
        );
    }

    private String replaceVariables(String template, Map<String, Object> variables) {
        String result = template;

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(
                    placeholder,
                    String.valueOf(entry.getValue())
            );
        }

        return result;
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

// ... inside EmailService class

    public void sendTaskAssignedEmail(TaskAssignedEvent event) {
        try {
            log.info("1. Task Assigned Listener triggered!");
            log.info("2. Processing email for user: {}", event.getUserEmail());

            // 1. Define the template file name
            // Ensure 'src/main/resources/templates/task-assigned.html' exists
            String templateCode = "task-assigned";

            // 2. Load HTML template using your existing helper method
            String htmlTemplate = loadEmailTemplate(templateCode);

            // 3. Format the Timestamp
            // Raw LocalDateTime looks like '2024-12-24T14:30:00'. We want it readable.
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm a");
            String formattedDate = event.getTimestamp() != null
                    ? event.getTimestamp().format(formatter)
                    : "N/A";

            // 4. Prepare Variables for Replacement {{key}}
            Map<String, Object> variables = new HashMap<>();
            variables.put("taskId", event.getTaskId());
            variables.put("userEmail", event.getUserEmail());
            variables.put("assignedUserId", event.getAssignedUserId());
            variables.put("taskTitle", event.getTaskTitle());
            variables.put("userFullName", event.getUserFullName());
            variables.put("timestamp", formattedDate);

            // 5. Replace variables using your existing helper method
            String processedHtml = replaceVariables(htmlTemplate, variables);

            // 6. Create and Send Email
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(event.getUserEmail());
            helper.setSubject("New Task Assigned: " + event.getTaskTitle());
            helper.setText(processedHtml, true); // true = Is HTML
            helper.setFrom("system@yourcompany.com"); // Optional: set your sender

            javaMailSender.send(message);

            log.info("Task assigned email sent successfully to {}", event.getUserEmail());

        } catch (Exception e) {
            log.error("Failed to send task assignment email to {}", event.getUserEmail(), e);
            // Throwing exception allows RabbitMQ to retry if configured
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
