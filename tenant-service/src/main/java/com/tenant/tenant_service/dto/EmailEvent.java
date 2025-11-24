package com.tenant.tenant_service.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent {
    private NotificationEventType type;
    private String email;
    private String subject;
    private String message;
    private Map<String, Object> metadata;
}
