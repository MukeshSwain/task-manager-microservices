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
    private final UserClient userClient;
    private final TaskClient taskClient;
    private final NotificationProducer notificationProducer;



    public ProjectServiceImpl(TenantClient tenantClient, ProjectRepository projectRepository, ProjectMemberRepository memberRepository, UserClient userClient, TaskClient taskClient, NotificationProducer notificationProducer) {
        this.tenantClient = tenantClient;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userClient = userClient;
        this.taskClient = taskClient;
        this.notificationProducer = notificationProducer;
    }
    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest req) {

        MemberResponse owner = tenantClient.getMember(req.getOrgId(), req.getOwnerAuthId());
        if (owner == null) throw new NotFoundException("Owner not found in organization");

        validateOwnerRole(owner);
        boolean isSameUser = req.getOwnerAuthId().equals(req.getTeamLeadAuthId());
        MemberResponse teamLead = isSameUser ? owner : tenantClient.getMember(req.getOrgId(), req.getTeamLeadAuthId());

        if (teamLead == null) throw new NotFoundException("Team lead not found in organization");
        try {
            Project project = Project.builder()
                    .name(req.getName())
                    .description(req.getDescription())
                    .orgId(req.getOrgId())
                    .ownerAuthId(req.getOwnerAuthId())
                    .teamLeadAuthId(req.getTeamLeadAuthId())
                    .priority(Priority.valueOf(req.getPriority().toUpperCase()))
                    .status(Status.valueOf(req.getStatus().toUpperCase()))
                    .deadline(req.getDeadline())
                    .memberCount(isSameUser ? 1 : 2)
                    .build();

            Project saved = projectRepository.save(project);
            saveProjectMember(saved.getId(), req.getOwnerAuthId(), Role.OWNER);
            if (!isSameUser) {
                saveProjectMember(saved.getId(), req.getTeamLeadAuthId(), Role.LEAD);
            }
            try {
                sendCreationNotification(saved, owner, "Project Created Successfully",
                        "Your new project has been successfully initialized.", owner.getName(), teamLead.getName());
                if (!isSameUser) {
                    sendCreationNotification(saved, teamLead, "New Assignment: Team Lead",
                            "You have been assigned as the Team Lead for a new project.", owner.getName(), teamLead.getName());
                }
            } catch (Exception e) {
                log.error("Project created but notification failed", e);
            }

            return Mapping.toProjectResponse(saved);

        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid Priority or Status value provided");
        }
    }

    private void saveProjectMember(String projectId, String authId, Role role) {
        memberRepository.save(ProjectMember.builder()
                .projectId(projectId)
                .authId(authId)
                .role(role)
                .build());
    }

    private void validateOwnerRole(MemberResponse owner) {
        if (owner.getRole() == null) throw new BadRequestException("Owner has no role assigned");
        try {
            Role role = Role.valueOf(owner.getRole().toUpperCase());
            if (role != Role.ADMIN && role != Role.OWNER) {
                throw new BadRequestException("You are not authorized to create a project");
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role format: " + owner.getRole());
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

        // --- 1. VALIDATION ---
        Project project = projectRepository.findByIdAndDeletedFalse(projectId);
        if (project == null) throw new NotFoundException("Project not found");

        MemberResponse actor = tenantClient.getMember(project.getOrgId(), performedBy);
        if (actor == null) throw new NotFoundException("User not found");

        // Validate Permissions (Admin or Owner only)
        try {
            Role actorRole = Role.valueOf(actor.getRole().toUpperCase());
            if (actorRole != Role.ADMIN && actorRole != Role.OWNER) {
                throw new BadRequestException("You are not authorized to update this project");
            }
        } catch (Exception e) {
            throw new BadRequestException("Invalid or missing role for user");
        }

        boolean changed = false;

        // --- 2. UPDATE BASIC FIELDS ---
        if (req.getName() != null && !req.getName().equals(project.getName())) {
            project.setName(req.getName());
            changed = true;
        }
        if (req.getDescription() != null && !req.getDescription().equals(project.getDescription())) {
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
                throw new BadRequestException("Invalid Priority value");
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
                throw new BadRequestException("Invalid Status value");
            }
        }
        if (req.getDeadline() != null && !req.getDeadline().equals(project.getDeadline())) {
            project.setDeadline(req.getDeadline());
            changed = true;
        }

        // --- 3. TEAM LEAD SWAP LOGIC ---
        // We only run this block (and send the email) if the Lead ID is actually different
        if (req.getTeamLeadAuthId() != null && !req.getTeamLeadAuthId().equals(project.getTeamLeadAuthId())) {

            String oldLeadId = project.getTeamLeadAuthId();
            String newLeadId = req.getTeamLeadAuthId();

            MemberResponse newLeadResp = tenantClient.getMember(project.getOrgId(), newLeadId);
            if (newLeadResp == null) {
                throw new BadRequestException("New Team Lead must be a member of the organization");
            }

            // A. Demote Old Lead (if not Owner)
            if (!oldLeadId.equals(project.getOwnerAuthId())) {
                ProjectMember oldLead = memberRepository.findByProjectIdAndAuthId(projectId, oldLeadId);
                if (oldLead != null) {
                    oldLead.setRole(Role.COLLABORATOR);
                    memberRepository.save(oldLead);
                }
            } else {
                // If Old Lead was Owner, ensure they stay Owner (Owner > Lead)
                ProjectMember oldLead = memberRepository.findByProjectIdAndAuthId(projectId, oldLeadId);
                if (oldLead != null && oldLead.getRole() != Role.OWNER) {
                    oldLead.setRole(Role.OWNER);
                    memberRepository.save(oldLead);
                }
            }

            // B. Promote New Lead
            if (!newLeadId.equals(project.getOwnerAuthId())) {
                ProjectMember newLeadMember = memberRepository.findByProjectIdAndAuthId(projectId, newLeadId);

                if (newLeadMember == null) {
                    // Create new member entry
                    newLeadMember = ProjectMember.builder()
                            .projectId(projectId)
                            .authId(newLeadId)
                            .role(Role.LEAD)
                            .build();
                    memberRepository.save(newLeadMember);
                    project.setMemberCount(project.getMemberCount() + 1);
                } else {
                    // Update existing member
                    newLeadMember.setRole(Role.LEAD);
                    memberRepository.save(newLeadMember);
                }
            }

            project.setTeamLeadAuthId(newLeadId);
            changed = true;

            // --- 4. SEND EMAIL NOTIFICATION (Only when Lead changes) ---
            try {
                EmailRequest emailRequest = new EmailRequest();
                Map<String, Object> emailVars = new HashMap<>();

                String dashboardUrl = String.format("http://localhost:5173/orgs/%s/projects", project.getOrgId());

                emailVars.put("projectName", project.getName());
                emailVars.put("performedBy", actor.getName());
                emailVars.put("dashboardLink", dashboardUrl);
                emailVars.put("recipientName", newLeadResp.getName());
                emailVars.put("dueDate", project.getDeadline() != null ? project.getDeadline().toString() : "No Deadline");

                emailRequest.setSubject("New Assignment: You are now Team Lead");
                emailRequest.setTemplateCode("new-lead-assigned"); // Matches new-lead-assigned.html
                emailRequest.setVariables(emailVars);
                emailRequest.setToEmail(newLeadResp.getEmail());

                // Use the dedicated Routing Key
                notificationProducer.sendEvent(emailRequest, RabbitConfig.NEW_LEAD_ASSIGNED_KEY);

            } catch (Exception e) {
                log.error("Failed to send Team Lead notification email", e);
                // We do NOT throw exception here; we let the DB update succeed.
            }
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
    private void sendCreationNotification(Project project, MemberResponse recipient, String subject,
                                          String messageBody, String ownerName, String leadName) {

        // 1. Construct URL properly using injected value
        String dashboardUrl = String.format("http://localhost:5173/orgs/%s/projects", project.getOrgId());

        // 2. Map variables for the HTML template
        Map<String, Object> emailVars = new HashMap<>();
        emailVars.put("recipientName", recipient.getName());
        emailVars.put("messageBody", messageBody);
        emailVars.put("projectName", project.getName());
        emailVars.put("projectId", project.getId());
        emailVars.put("priority", project.getPriority().toString());
        emailVars.put("deadline", project.getDeadline().toString());
        emailVars.put("ownerName", ownerName);     // Passed explicitly so it's accurate
        emailVars.put("teamLeadName", leadName);   // Passed explicitly
        emailVars.put("dashboardLink", dashboardUrl);

        // 3. Build Request
        EmailRequest emailEvent = EmailRequest.builder()
                .toEmail(recipient.getEmail())
                .subject("Project Update: " + subject)
                .templateCode("project-created") // Matches filename project-created.html
                .variables(emailVars)
                .build();

        // 4. Send to Queue
        notificationProducer.sendEvent(emailEvent, RabbitConfig.PROJECT_CREATED_KEY);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
