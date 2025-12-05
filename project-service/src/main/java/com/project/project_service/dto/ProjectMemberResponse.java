package com.project.project_service.dto;

import com.project.project_service.model.Role;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectMemberResponse {
    private String id;
    private String projectId;
    private String authId;
    private Role role;
    OffsetDateTime joinedAt;
}
