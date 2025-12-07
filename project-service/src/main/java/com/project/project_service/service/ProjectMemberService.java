package com.project.project_service.service;

import com.project.project_service.dto.AddMemberRequest;
import com.project.project_service.dto.ProjectMemberResponse;
import com.project.project_service.dto.UpdateMemberRoleRequest;
import java.util.List;


public interface ProjectMemberService {
    ProjectMemberResponse addMember(String projectId, AddMemberRequest request, String performedBy);
    ProjectMemberResponse removeMember(String projectId, String authId, String performedBy);
    ProjectMemberResponse updateRole(String projectId, String authId, UpdateMemberRoleRequest req, String performedBy);
    List<ProjectMemberResponse> listMembers(String projectId);
}
