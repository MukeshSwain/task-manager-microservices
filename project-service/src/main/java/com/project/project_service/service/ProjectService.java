package com.project.project_service.service;

import com.project.project_service.dto.CreateProjectRequest;
import com.project.project_service.dto.ProjectResponse;
import com.project.project_service.dto.UpdateProjectRequest;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest projectRequest);
    ProjectResponse getProject(String projectId);
    List<ProjectResponse> listByOrg(String orgId);
    List<ProjectResponse> listByUser(String authId);
    ProjectResponse updateProject(String projectId, UpdateProjectRequest req,String performedBy);
    void softDeleteProject(String projectId);
}
