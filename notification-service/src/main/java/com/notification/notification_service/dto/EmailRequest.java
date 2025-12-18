package com.notification.notification_service.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailRequest {
    private String toEmail;


    private String subject;


    // e.g., "project-welcome.html"
    private String templateCode;

    // Dynamic data to replace placeholders like {{projectName}} in the HTML
    private Map<String, Object> variables;
}
