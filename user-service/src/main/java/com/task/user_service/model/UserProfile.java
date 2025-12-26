package com.task.user_service.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_profile")
@Builder
public class UserProfile {
    @Id
    private String id;
    @Builder.Default
    private Role role = Role.MEMBER;
    private String authId;
    private String email;
    private String name;
    private String bio;
    private String avatarUrl;
    private String avatarPublicId;
    private Boolean isEmailVerified;
    private Map<String, Boolean> notificationPref;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
