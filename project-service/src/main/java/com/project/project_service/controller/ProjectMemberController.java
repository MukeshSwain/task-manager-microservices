package com.project.project_service.controller;

import com.project.project_service.dto.AddMemberRequest;
import com.project.project_service.dto.ProjectMemberResponse;
import com.project.project_service.service.ProjectMemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }
    @PostMapping("/{performedBy}")
    public ResponseEntity<ProjectMemberResponse> addMember(@PathVariable String projectId, @Valid @RequestBody AddMemberRequest request, @PathVariable String performedBy){
        return ResponseEntity.ok(projectMemberService.addMember(projectId, request, performedBy));
    }
}
