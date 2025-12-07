package com.project.project_service.service.impl;

import com.project.project_service.dto.AddMemberRequest;
import com.project.project_service.dto.ProjectMemberResponse;
import com.project.project_service.dto.UpdateMemberRoleRequest;
import com.project.project_service.exception.BadRequestException;
import com.project.project_service.exception.NotFoundException;
import com.project.project_service.feign.UserClient;
import com.project.project_service.mapping.Mapping;
import com.project.project_service.model.Project;
import com.project.project_service.model.ProjectMember;
import com.project.project_service.model.Role;
import com.project.project_service.repository.ProjectMemberRepository;
import com.project.project_service.repository.ProjectRepository;
import com.project.project_service.service.ProjectMemberService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final UserClient userClient;
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;


    public ProjectMemberServiceImpl(ProjectMemberRepository projectMemberRepository, UserClient userClient, ProjectRepository projectRepository, EntityManager entityManager) {
        this.projectMemberRepository = projectMemberRepository;
        this.userClient = userClient;
        this.projectRepository = projectRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ProjectMemberResponse addMember(String projectId, AddMemberRequest request, String performedBy) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId);
        if (project == null){
            throw new NotFoundException("Project not found!");
        }
        ProjectMember actor = projectMemberRepository.findByProjectIdAndAuthId(projectId, performedBy);
        if(actor.getRole() != Role.OWNER){
            throw new BadRequestException("Only owner can add members");
        }
        if(projectMemberRepository.findByProjectIdAndAuthId(projectId, request.getAuthId())!=null){
            throw new BadRequestException("Member already exists!");
        }
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(projectId)
                .authId(request.getAuthId())
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .build();

        ProjectMember savedProjectMember = projectMemberRepository.saveAndFlush(projectMember);
        entityManager.refresh(savedProjectMember);

        //Todo : notification

        return Mapping.toProjectMemberResponse(savedProjectMember);
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
