package com.project.project_service.service.impl;

import com.project.project_service.dto.AddMemberRequest;
import com.project.project_service.dto.ProjectMemberResponse;
import com.project.project_service.dto.UpdateMemberRoleRequest;
import com.project.project_service.service.ProjectMemberService;

import java.util.List;

public class ProjectMemberServiceImpl implements ProjectMemberService {
    @Override
    public ProjectMemberResponse addMember(String projectId, AddMemberRequest request, String performedBy) {
        return null;
    }

    @Override
    public ProjectMemberResponse removeMember(String projectId, String authId, String performedBy) {
        return null;
    }

    @Override
    public ProjectMemberResponse updateRole(String projectId, String authId, UpdateMemberRoleRequest req, String performedBy) {
        return null;
    }

    @Override
    public List<ProjectMemberResponse> listMembers(String projectId) {
        return List.of();
    }
}
