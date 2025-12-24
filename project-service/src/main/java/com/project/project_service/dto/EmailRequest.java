package com.project.project_service.dto;

import java.util.Map;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {
    private String toEmail;
    private String subject;
    private String templateCode;
    private Map<String, Object> variables;
}
