package com.project.project_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetail {
    private String id;
    private String role;
    private String authId;
    private String email;
    private String name;
    private String bio;
    private String avatarUrl;
    private boolean isEmailVerified;
    private Map<String, Boolean> notificationPref;
    private LocalDateTime createdAt;
}
