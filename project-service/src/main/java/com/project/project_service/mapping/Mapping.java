package com.project.project_service.mapping;

import com.project.project_service.dto.ProjectResponse;
import com.project.project_service.model.Project;

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
}
