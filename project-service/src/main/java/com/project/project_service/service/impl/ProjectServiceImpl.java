package com.project.project_service.service.impl;

import com.project.project_service.dto.CreateProjectRequest;
import com.project.project_service.dto.MemberResponse;
import com.project.project_service.dto.ProjectResponse;
import com.project.project_service.exception.BadRequestException;
import com.project.project_service.exception.NotFoundException;
import com.project.project_service.feign.TenantClient;
import com.project.project_service.mapping.Mapping;
import com.project.project_service.model.Project;
import com.project.project_service.model.ProjectMember;
import com.project.project_service.model.Role;
import com.project.project_service.repository.ProjectMemberRepository;
import com.project.project_service.repository.ProjectRepository;
import com.project.project_service.service.ProjectService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {
    private final TenantClient tenantClient;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final EntityManager entityManager;

    public ProjectServiceImpl(TenantClient tenantClient, ProjectRepository projectRepository, ProjectMemberRepository memberRepository, EntityManager entityManager) {
        this.tenantClient = tenantClient;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest projectRequest) {
        MemberResponse existMember = tenantClient.getMember(projectRequest.getOrgId(), projectRequest.getOwnerAuthId());
        if(existMember == null){
            throw new NotFoundException("Member not found");
        }
        if (!Role.valueOf(existMember.getRole()).equals(Role.ADMIN) && !Role.valueOf(existMember.getRole()).equals(Role.OWNER)) {
            throw new BadRequestException("You are not authorized to create project");
        }

        Project project = Project.builder()
                .name(projectRequest.getName())
                .description(projectRequest.getDescription())
                .orgId(projectRequest.getOrgId())
                .ownerAuthId(projectRequest.getOwnerAuthId())
                .build();
        Project savedProject = projectRepository.saveAndFlush(project);
        entityManager.refresh(savedProject);
        //add owner as project member
        ProjectMember ownerMember = ProjectMember.builder()
                .projectId(savedProject.getId())
                .authId(projectRequest.getOwnerAuthId())
                .role(Role.OWNER)
                .build();
        memberRepository.save(ownerMember);

        // Publish event async (optional) todo
        return Mapping.toProjectResponse(savedProject);
    }

    @Override
    public ProjectResponse getProject(String projectId) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId);
        if (project == null){
            throw new NotFoundException("Project not found");
        }
        return Mapping.toProjectResponse(project);
    }

    @Override
    public List<ProjectResponse> listByOrg(String orgId) {
        List<Project> projects= projectRepository.findAllByOrgIdAndDeletedFalse(orgId);
        return projects.stream()
                .map(project -> Mapping.toProjectResponse(project)).toList();
    }

    @Override
    public List<ProjectResponse> listByUser(String authId) {
        return List.of();
    }

    @Override
    public ProjectResponse updateProject(String projectId, CreateProjectRequest req) {
        return null;
    }

    @Override
    public void softDeleteProject(String projectId) {

    }
}
