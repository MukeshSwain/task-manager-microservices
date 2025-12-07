package com.project.project_service.mapping;

import com.project.project_service.dto.ProjectMemberResponse;
import com.project.project_service.dto.ProjectResponse;
import com.project.project_service.model.Project;
import com.project.project_service.model.ProjectMember;

public class Mapping {
    public static ProjectResponse toProjectResponse(Project project){
        return ProjectResponse.builder()
                .id(project.getId())
                .orgId(project.getOrgId())
                .ownerAuthId(project.getOwnerAuthId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public static ProjectMemberResponse toProjectMemberResponse(ProjectMember projectMember){
        return ProjectMemberResponse.builder()
                .id(projectMember.getId())
                .projectId(projectMember.getProjectId())
                .authId(projectMember.getAuthId())
                .role(projectMember.getRole())
                .joinedAt(projectMember.getJoinedAt())
                .build();
    }
}
