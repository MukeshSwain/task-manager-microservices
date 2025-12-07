package com.project.project_service.controller;

import com.project.project_service.dto.AddMemberRequest;
import com.project.project_service.dto.ProjectMemberResponse;
import com.project.project_service.dto.UpdateMemberRoleRequest;
import com.project.project_service.service.ProjectMemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @DeleteMapping("/{authId}/{performedBy}")
    public ResponseEntity<Void> removeMember(@PathVariable String projectId,@PathVariable String authId, @PathVariable String performedBy ){
        projectMemberService.removeMember(projectId, authId, performedBy);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PutMapping("/{authId}/{performedBy}")
    public ResponseEntity<ProjectMemberResponse> updateRole(@PathVariable String projectId,@PathVariable String authId, @PathVariable String performedBy, @Valid @RequestBody UpdateMemberRoleRequest req){
        return ResponseEntity.ok(projectMemberService.updateRole(projectId, authId, req, performedBy));
    }
    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> listMembers(@PathVariable String projectId){
        return ResponseEntity.ok(projectMemberService.listMembers(projectId));
    }
}
