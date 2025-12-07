package com.project.project_service.controller;


import com.project.project_service.dto.CreateProjectRequest;
import com.project.project_service.dto.ProjectResponse;
import com.project.project_service.dto.UpdateProjectRequest;
import com.project.project_service.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:5173",
        allowCredentials = "true")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest projectRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(projectRequest));
    }
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable String projectId){
        return ResponseEntity.ok(projectService.getProject(projectId));
    }
    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<ProjectResponse>> listByOrg(@PathVariable String orgId){
        return ResponseEntity.ok(projectService.listByOrg(orgId));
    }
    @GetMapping("/user/{authId}")
    public ResponseEntity<List<ProjectResponse>> listByUser(@PathVariable String authId){
        return ResponseEntity.ok(projectService.listByUser(authId));
    }
    @PutMapping("/{projectId}/{performedBy}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable String projectId, @RequestBody UpdateProjectRequest req,@PathVariable String performedBy){
        return ResponseEntity.ok(projectService.updateProject(projectId, req,performedBy));
    }
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId){
        projectService.softDeleteProject(projectId);
        return ResponseEntity.ok().build();
    }


}
