package com.project.project_service.service;

import com.project.project_service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest projectRequest);
    ProjectResponse getProject(String projectId);
    List<ProjectResponse> listByOrg(String orgId);
    List<ProjectDetailResponse> listByUser(String authId);
    ProjectResponse updateProject(String projectId, UpdateProjectRequest req,String performedBy);
    void softDeleteProject(String projectId);

    Boolean validate(String projectId);

    Page<TaskResponse> getTasksByOrg(String orgId, Pageable pageable);
}
