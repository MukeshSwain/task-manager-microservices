package com.project.project_service.dto;

import com.project.project_service.model.Role;
import com.project.project_service.model.Status;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class ProjectMemberResponse {

    private String id;
    private String projectId;

    // Use an Enum, not a String, for type safety
    private Role role;
    private OffsetDateTime joinedAt;

    // Replaces 'authId'. Contains the populated user data.
    private UserSummary user;

    @Data
    @Builder
    public static class UserSummary {
        private String authId;
        private String name;
        private String email;
        private String avatarUrl;
        private String orgRole;
    }
}