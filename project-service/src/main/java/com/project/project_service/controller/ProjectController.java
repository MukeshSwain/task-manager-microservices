package com.project.project_service.controller;


import com.project.project_service.dto.CreateProjectRequest;
import com.project.project_service.dto.ProjectResponse;
import com.project.project_service.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }
    @PostMapping
    public ProjectResponse createProject(@Valid @RequestBody CreateProjectRequest projectRequest){
        return projectService.createProject(projectRequest);
    }
}
