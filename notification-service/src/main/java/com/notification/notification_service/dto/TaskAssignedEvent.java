package com.notification.notification_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignedEvent {
    private String taskId;
    private String userEmail;
    private String assignedUserId;
    private String taskTitle;
    private String userFullName;
    private LocalDateTime timestamp;
}
