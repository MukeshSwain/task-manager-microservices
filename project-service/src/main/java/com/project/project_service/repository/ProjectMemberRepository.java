package com.project.project_service.repository;

import com.project.project_service.dto.ProjectMemberResponse;
import com.project.project_service.model.ProjectMember;
import com.project.project_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, String> {
    List<ProjectMember> findByAuthId(String authId);

    ProjectMember findByProjectIdAndAuthId(String projectId, String performedBy);

    long countByProjectIdAndRole(String projectId, Role role);

    List<ProjectMember> findByProjectId(String projectId);
}
