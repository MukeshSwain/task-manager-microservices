package com.project.project_service.dto;

import lombok.*;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDetailResponse {
    private String id;
    private String name;
    private String description;
    private String status;
    private String orgId;
    private String priority;
    private String deadline;
    private String createdAt;
    private String updatedAt;

    private UserSummary owner;
    private UserSummary teamLead;

    // Use a shared static class for user details to avoid code duplication
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummary {
        private String authId;
        private String name;
        private String email;
        private String avatarUrl;
    }
}