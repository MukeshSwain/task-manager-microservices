package com.project.project_service.service.impl;

import com.project.project_service.config.RabbitConfig;
import com.project.project_service.dto.*;
import com.project.project_service.exception.BadRequestException;
import com.project.project_service.exception.NotFoundException;
import com.project.project_service.feign.TaskClient;
import com.project.project_service.feign.TenantClient;
import com.project.project_service.feign.UserClient;
import com.project.project_service.mapping.Mapping;
import com.project.project_service.messaging.NotificationProducer;
import com.project.project_service.model.*;
import com.project.project_service.repository.ProjectMemberRepository;
import com.project.project_service.repository.ProjectRepository;
import com.project.project_service.service.ProjectService;
import jakarta.persistence.EntityManager;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    private final TenantClient tenantClient;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final EntityManager entityManager;
    private final UserClient userClient;
    private final TaskClient taskClient;
    private final NotificationProducer notificationProducer;



    public ProjectServiceImpl(TenantClient tenantClient, ProjectRepository projectRepository, ProjectMemberRepository memberRepository, EntityManager entityManager, UserClient userClient, TaskClient taskClient, NotificationProducer notificationProducer) {
        this.tenantClient = tenantClient;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.entityManager = entityManager;
        this.userClient = userClient;
        this.taskClient = taskClient;
        this.notificationProducer = notificationProducer;
    }

    @Override
    @Transactional // CRITICAL: Ensures DB integrity if any save fails
    public ProjectResponse createProject(CreateProjectRequest req) {
        // --- 1. VALIDATION ---
        MemberResponse owner = tenantClient.getMember(req.getOrgId(), req.getOwnerAuthId());
        if (owner == null) {
            throw new NotFoundException("Owner not found in organization");
        }

        // Fixed logging bug (was logging Name as Role)
        log.info("Owner: {}, Role: {}", owner.getEmail(), owner.getRole());

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

        // --- 2. FETCH TEAM LEAD ---
        boolean isSameUser = req.getOwnerAuthId().equals(req.getTeamLeadAuthId());
        MemberResponse teamLead;

        if (isSameUser) {
            teamLead = owner;
        } else {
            teamLead = tenantClient.getMember(req.getOrgId(), req.getTeamLeadAuthId());
            if (teamLead == null) {
                throw new NotFoundException("Team lead not found in organization");
            }
        }

        // --- 3. BUILD & SAVE PROJECT ---
        try {
            Project project = Project.builder()
                    .name(req.getName())
                    .description(req.getDescription())
                    .orgId(req.getOrgId())
                    .ownerAuthId(req.getOwnerAuthId())
                    .teamLeadAuthId(req.getTeamLeadAuthId())
                    // Safe Enum Parsing
                    .priority(Priority.valueOf(req.getPriority().toUpperCase()))
                    .status(Status.valueOf(req.getStatus().toUpperCase()))
                    .deadline(req.getDeadline())
                    .memberCount(isSameUser ? 1 : 2)
                    .build();

            Project saved = projectRepository.saveAndFlush(project);
            entityManager.refresh(saved);

            // --- 4. SAVE MEMBERS (Inline) ---
            // Save Owner
            memberRepository.save(ProjectMember.builder()
                    .projectId(saved.getId())
                    .authId(req.getOwnerAuthId())
                    .role(Role.OWNER)
                    .build());

            // Save Team Lead (if different)
            if (!isSameUser) {
                memberRepository.save(ProjectMember.builder()
                        .projectId(saved.getId())
                        .authId(req.getTeamLeadAuthId())
                        .role(Role.LEAD)
                        .build());
            }

            // --- 5. NOTIFICATIONS ---
            // Wrapped in try-catch so email failures DO NOT rollback the successful DB transaction
            try {
                // Notify Owner
                sendCreationNotification(saved, owner, "Project Created Successfully");

                // Notify Team Lead (if different)
                if (!isSameUser) {
                    sendCreationNotification(saved, teamLead, "You have been assigned as Team Lead");
                }
            } catch (Exception e) {
                log.error("Project created, but failed to send notifications", e);
            }

            return Mapping.toProjectResponse(saved);

        } catch (IllegalArgumentException e) {
            // Catches invalid Priority/Status enum errors
            throw new BadRequestException("Invalid Priority or Status value provided");
        }
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
    public List<ProjectDetailResponse> listByUser(String authId) {
        // 1. Fetch Projects (Database)
        List<ProjectMember> memberships = memberRepository.findByAuthId(authId);
        List<String> projectIds = memberships.stream().map(ProjectMember::getProjectId).toList();

        if (projectIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Project> projectList = projectRepository.findAllByIdInAndDeletedFalse(projectIds);

        // 2. Collect ALL unique Auth IDs (Owner + TeamLead)
        Set<String> userIdsToFetch = new HashSet<>();
        projectList.forEach(p -> {
            if (p.getOwnerAuthId() != null) userIdsToFetch.add(p.getOwnerAuthId());
            if (p.getTeamLeadAuthId() != null) userIdsToFetch.add(p.getTeamLeadAuthId());
        });

        // 3. Batch Fetch Users (ONE Network Call)
        // Assuming userClient.getUsersByIds returns a list of UserDTOs
        List<UserDetail> users = userClient.getUsersByIds(new ArrayList<>(userIdsToFetch));

        // 4. Convert List to Map for fast O(1) lookup
        // Map<AuthId, UserResponse>
        Map<String, UserDetail> userMap = users.stream()
                .collect(Collectors.toMap(UserDetail::getAuthId, u -> u));

        // 5. Map Projects to DTOs (In Memory - Very Fast)
        return projectList.stream()
                .map(project -> {
                    UserDetail owner = userMap.get(project.getOwnerAuthId());
                    UserDetail lead = userMap.get(project.getTeamLeadAuthId());

                    return ProjectDetailResponse.builder()
                            .id(project.getId())
                            .name(project.getName())
                            .description(project.getDescription())
                            .status(project.getStatus().name())
                            .orgId(project.getOrgId())
                            .priority(project.getPriority().name())
                            .deadline(project.getDeadline() != null ? project.getDeadline().toString() : null)
                            .createdAt(project.getCreatedAt().toString())
                            .updatedAt(project.getUpdatedAt().toString())
                            // Map Owner
                            .owner(owner != null ? ProjectDetailResponse.UserSummary.builder()
                                    .authId(owner.getAuthId())
                                    .name(owner.getName())
                                    .email(owner.getEmail())
                                    .avatarUrl(owner.getAvatarUrl())
                                    .build() : null)
                            // Map Team Lead
                            .teamLead(lead != null ? ProjectDetailResponse.UserSummary.builder()
                                    .authId(lead.getAuthId())
                                    .name(lead.getName())
                                    .email(lead.getEmail())
                                    .avatarUrl(lead.getAvatarUrl())
                                    .build() : null)
                            .build();
                })
                .toList();
    }
    @Override
    @Transactional
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest req, String performedBy) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId);
        if (project == null) {
            throw new NotFoundException("Project not found");
        }
        MemberResponse actor = tenantClient.getMember(project.getOrgId(), performedBy);
        if (actor == null) {
            throw new NotFoundException("User not found");
        }

        Role actorRole;
        try {
            actorRole = Role.valueOf(actor.getRole().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid or missing role for user");
        }

        if (actorRole != Role.ADMIN && actorRole != Role.OWNER) {
            throw new BadRequestException("You are not authorized to update this project");
        }

        boolean changed = false;
        if (notBlank(req.getName()) && !req.getName().equals(project.getName())) {
            project.setName(req.getName());
            changed = true;
        }

        if (notBlank(req.getDescription()) && !req.getDescription().equals(project.getDescription())) {
            project.setDescription(req.getDescription());
            changed = true;
        }

        if (req.getPriority() != null) {
            try {
                Priority p = Priority.valueOf(req.getPriority().toUpperCase());
                if (p != project.getPriority()) {
                    project.setPriority(p);
                    changed = true;
                }
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid Priority value: " + req.getPriority());
            }
        }

        if (req.getStatus() != null) {
            try {
                Status s = Status.valueOf(req.getStatus().toUpperCase());
                if (s != project.getStatus()) {
                    project.setStatus(s);
                    changed = true;
                }
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid Status value: " + req.getStatus());
            }
        }

        if (req.getDeadline() != null && !req.getDeadline().equals(project.getDeadline())) {
            project.setDeadline(req.getDeadline());
            changed = true;
        }
        if (req.getTeamLeadAuthId() != null &&
                !req.getTeamLeadAuthId().equals(project.getTeamLeadAuthId())) {
            MemberResponse newLeadResp = tenantClient.getMember(project.getOrgId(), req.getTeamLeadAuthId());
            if (newLeadResp == null) {
                throw new BadRequestException("New Team Lead must be a member of the organization");
            }

            String oldLeadId = project.getTeamLeadAuthId();
            String newLeadId = req.getTeamLeadAuthId();

            ProjectMember oldLead = memberRepository.findByProjectIdAndAuthId(projectId, oldLeadId);
            if (!oldLeadId.equals(project.getOwnerAuthId())) {
                // Old lead is NOT owner → demote to collaborator
                if (oldLead != null) {
                    oldLead.setRole(Role.COLLABORATOR);
                    memberRepository.save(oldLead);
                }
            } else {
                if (oldLead != null && oldLead.getRole() != Role.OWNER) {
                    oldLead.setRole(Role.OWNER);
                    memberRepository.save(oldLead);
                }
            }
            if (!newLeadId.equals(project.getOwnerAuthId())) {

                ProjectMember newLeadMember =
                        memberRepository.findByProjectIdAndAuthId(projectId, newLeadId);

                if (newLeadMember == null) {
                    newLeadMember = ProjectMember.builder()
                            .projectId(projectId)
                            .authId(newLeadId)
                            .role(Role.LEAD)
                            .build();

                    memberRepository.save(newLeadMember);
                    project.setMemberCount(project.getMemberCount() + 1);
                } else {
                    newLeadMember.setRole(Role.LEAD);
                    memberRepository.save(newLeadMember);
                }
            }
            project.setTeamLeadAuthId(newLeadId);
            changed = true;
        }
        if (!changed) {
            return Mapping.toProjectResponse(project);
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

    @Override
    public Boolean validate(String projectId) {
        return projectRepository.existsById(projectId);
    }

    @Override
    public Page<TaskResponse> getTasksByOrg(String orgId, Pageable pageable) {
        if (!tenantClient.validate(orgId)) {
            throw new NotFoundException("Organization not found with id " + orgId);
        }

        // 2. Security Check: Fetch all valid Project IDs for this Org
        List<String> validOrgProjectIds = projectRepository.findIdsByOrgIdAndDeletedFalse(orgId);

        // UX Improvement: Return empty page instead of 404 if org has no projects
        if (validOrgProjectIds.isEmpty()) {
            log.info("No projects found for orgId: {}. Returning empty.", orgId);
            return Page.empty(pageable);
        }



        log.info("Fetching task page {} for {} projects in Org {}", pageable.getPageNumber(), validOrgProjectIds.size(), orgId);

        // 4. Call Downstream Client with Pagination
        // Note: ensure your taskClient handles the Page return type correctly (see step 2 below)
        return taskClient.getTasksByOrg(validOrgProjectIds,pageable);
    }
    private void sendCreationNotification(Project project, MemberResponse recipient, String subjectSuffix) {
        String dashboardUrl = String.format("http://localhost:5173/orgs/%s/projects", project.getOrgId());
        Map<String, Object> emailVars = new HashMap<>();
        emailVars.put("recipientName", recipient.getName());
        emailVars.put("projectName", project.getName());
        emailVars.put("projectId", project.getId());
        emailVars.put("ownerName", recipient.getName());
        emailVars.put("dashboardLink", dashboardUrl);

        EmailRequest emailEvent = EmailRequest.builder()
                .toEmail(recipient.getEmail())
                .subject("Project Notification: " + subjectSuffix)
                .templateCode("PROJECT_CREATION_TEMPLATE")
                .variables(emailVars)
                .build();

        notificationProducer.sendProjectCreatedEvent(emailEvent, RabbitConfig.PROJECT_CREATED_KEY);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
