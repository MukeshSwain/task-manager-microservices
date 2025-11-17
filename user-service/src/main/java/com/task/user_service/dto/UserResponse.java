package com.task.user_service.dto;

import com.task.user_service.model.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
public class UserResponse {
    private String id;
    private Role role;
    private String authId;
    private String email;
    private String name;
    private String bio;
    private String avatarUrl;
    private boolean isEmailVerified;
    private Map<String, Boolean> notificationPref;
    private LocalDateTime createdAt;

}
