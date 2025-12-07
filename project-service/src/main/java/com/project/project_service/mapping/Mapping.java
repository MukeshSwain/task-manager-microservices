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
                .memberCount(project.getMemberCount())
                .description(project.getDescription())
                .priority(project.getPriority())
                .status(project.getStatus())
                .deadline(project.getDeadline())
                .teamLeadAuthId(project.getTeamLeadAuthId())
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
