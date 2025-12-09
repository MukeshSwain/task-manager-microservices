package com.project.project_service.service.impl;

import com.project.project_service.dto.CreateProjectRequest;
import com.project.project_service.dto.MemberResponse;
import com.project.project_service.dto.ProjectResponse;
import com.project.project_service.dto.UpdateProjectRequest;
import com.project.project_service.exception.BadRequestException;
import com.project.project_service.exception.NotFoundException;
import com.project.project_service.feign.TenantClient;
import com.project.project_service.mapping.Mapping;
import com.project.project_service.model.*;
import com.project.project_service.repository.ProjectMemberRepository;
import com.project.project_service.repository.ProjectRepository;
import com.project.project_service.service.ProjectService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
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
    public ProjectResponse createProject(CreateProjectRequest req) {
        MemberResponse owner = tenantClient.getMember(req.getOrgId(), req.getOwnerAuthId());
        if (owner == null) {
            throw new NotFoundException("Owner not found in organization");
        }
        if (owner.getRole() == null) {
            throw new BadRequestException("Owner has no role assigned");
        }
        try {
            Role ownerRole = Role.valueOf(owner.getRole().toUpperCase());
            if (ownerRole != Role.ADMIN && ownerRole != Role.OWNER) {
                throw new BadRequestException("You are not authorized to create a project");
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role format: " + owner.getRole());
        }
        boolean isSameUser = req.getOwnerAuthId().equals(req.getTeamLeadAuthId());
        MemberResponse teamLead;

        if (isSameUser) {
            // Reuse the data we already fetched!
            teamLead = owner;
        } else {
            // Only fetch if it's a different person
            teamLead = tenantClient.getMember(req.getOrgId(), req.getTeamLeadAuthId());
            if (teamLead == null) {
                throw new NotFoundException("Team lead not found in organization");
            }
        }

        int memberCount = isSameUser ? 1 : 2;

        // 4. Create Project
        Project project = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .orgId(req.getOrgId())
                .ownerAuthId(req.getOwnerAuthId())
                .teamLeadAuthId(req.getTeamLeadAuthId())
                .priority(Priority.valueOf(req.getPriority().toUpperCase()))
                .status(Status.valueOf(req.getStatus().toUpperCase()))
                .deadline(req.getDeadline())
                .memberCount(memberCount)
                .build();

        Project saved = projectRepository.saveAndFlush(project);

        // Performance Note: Only keep this if you rely on DB triggers for timestamps.
        // If you use JPA @CreatedDate, remove this line to save a SELECT query.
        entityManager.refresh(saved);


        // Add Owner
        memberRepository.save(ProjectMember.builder()
                .projectId(saved.getId())
                .authId(req.getOwnerAuthId())
                .role(Role.OWNER)
                .build());

        // Add Team Lead (only if different)
        if (!isSameUser) {
            memberRepository.save(ProjectMember.builder()
                    .projectId(saved.getId())
                    .authId(req.getTeamLeadAuthId())
                    .role(Role.LEAD)
                    .build());
        }

        return Mapping.toProjectResponse(saved);
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
        List<ProjectMember> memberShips = memberRepository.findByAuthId(authId);
        List<ProjectResponse> projects = memberShips.stream()
                .map(member -> Mapping.toProjectResponse(projectRepository.findByIdAndDeletedFalse(member.getProjectId()))).toList();

        return projects;
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest req,String performedBy) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId);
        if(project == null){
            throw new NotFoundException("Project not found");
        }
        MemberResponse actor = tenantClient.getMember(project.getOrgId(),performedBy);
        if (!Role.valueOf(actor.getRole()).equals(Role.ADMIN) && !Role.valueOf(actor.getRole()).equals(Role.OWNER)) {
            throw new BadRequestException("You are not authorized to update project");
        }

        boolean changed=false;
        if (notBlank(req.getName()) && !req.getName().equals(project.getName())) {
            project.setName(req.getName());
            changed = true;
        }

        if (notBlank(req.getDescription()) && !req.getDescription().equals(project.getDescription())) {
            project.setDescription(req.getDescription());
            changed = true;
        }

        if (req.getPriority() != null && Priority.valueOf(req.getPriority().toUpperCase()) != project.getPriority()) {
            project.setPriority(Priority.valueOf(req.getPriority().toUpperCase()));

            changed = true;
        }

        if (req.getStatus() != null && Status.valueOf(req.getStatus().toUpperCase()) != project.getStatus()) {
            project.setStatus(Status.valueOf(req.getStatus().toUpperCase()));
            changed = true;
        }

        if (req.getDeadline() != null && !req.getDeadline().equals(project.getDeadline())) {
            project.setDeadline(req.getDeadline());
            changed = true;
        }
        if (req.getTeamLeadAuthId() != null && !req.getTeamLeadAuthId().equals(project.getTeamLeadAuthId())) {

            MemberResponse lead = tenantClient.getMember(project.getOrgId(), req.getTeamLeadAuthId());
            if (lead == null) {
                throw new BadRequestException("Team Lead must be a member of the organization");
            }

            project.setTeamLeadAuthId(req.getTeamLeadAuthId());
            changed = true;
        }
        if (!changed) {
            return Mapping.toProjectResponse(project); // no update needed
        }

        project.setUpdatedAt(OffsetDateTime.now());

        Project saved = projectRepository.save(project);

        return Mapping.toProjectResponse(saved);

    }

    @Override
    @Transactional
    public void softDeleteProject(String projectId) {
    Project project = projectRepository.findByIdAndDeletedFalse(projectId);
    if(project == null){
        throw new NotFoundException("Project not found");
    }
    project.setDeleted(true);
    project.setDeletedAt(OffsetDateTime.now());
    projectRepository.saveAndFlush(project);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
