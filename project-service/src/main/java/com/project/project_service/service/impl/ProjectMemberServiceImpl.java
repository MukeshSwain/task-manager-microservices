package com.project.project_service.service.impl;

import com.project.project_service.config.RabbitConfig;
import com.project.project_service.dto.*;
import com.project.project_service.exception.BadRequestException;
import com.project.project_service.exception.NotFoundException;
import com.project.project_service.feign.UserClient;
import com.project.project_service.messaging.NotificationProducer;
import com.project.project_service.model.Project;
import com.project.project_service.model.ProjectMember;
import com.project.project_service.model.Role;
import com.project.project_service.repository.ProjectMemberRepository;
import com.project.project_service.repository.ProjectRepository;
import com.project.project_service.service.ProjectMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final UserClient userClient;
    private final ProjectRepository projectRepository;
    private final NotificationProducer notificationProducer;

    public ProjectMemberServiceImpl(ProjectMemberRepository projectMemberRepository, UserClient userClient, ProjectRepository projectRepository, NotificationProducer notificationProducer) {
        this.projectMemberRepository = projectMemberRepository;
        this.userClient = userClient;
        this.projectRepository = projectRepository;
        this.notificationProducer = notificationProducer;

    }

    @Transactional // 1. Ensures data consistency (Atomicity)
    public ProjectMemberResponse addMember(String projectId, AddMemberRequest request, String performedBy) {

        // 2. Use Optional for better null handling
        Project project = projectRepository.findByIdAndDeletedFalse(projectId);
        if(project == null){
            throw new NotFoundException("Project not found!");
        }

        // 3. Optimize: Ensure DB has index on (projectId, authId)
        ProjectMember actor = projectMemberRepository.findByProjectIdAndAuthId(projectId, performedBy);
        if (actor == null || actor.getRole() != Role.OWNER) {
            throw new BadRequestException("Only owner can add members");
        }

        // 4. existsBy is faster than findBy if you don't need the object
        if (projectMemberRepository.existsByProjectIdAndAuthId(projectId, request.getAuthId())) {
            throw new BadRequestException("Member already exists!");
        }

        ProjectMember projectMember = ProjectMember.builder()
                .projectId(projectId)
                .authId(request.getAuthId())
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .build(); // Ensure @CreationTimestamp is in Entity so we don't need refresh()

        ProjectMember savedProjectMember = projectMemberRepository.save(projectMember);

        // Update count
        project.setMemberCount(project.getMemberCount() + 1);
        projectRepository.save(project);

        // Fetch User Details
        List<String> authIds = List.of(request.getAuthId(), performedBy);
        Map<String, UserDetail> userMap = new HashMap<>();

        // Safe check for external service response
        List<UserDetail> users = userClient.getUsersByIds(authIds);
        if (users != null) {
            users.forEach(u -> userMap.put(u.getAuthId(), u));
        }

        UserDetail user = userMap.get(request.getAuthId());
        UserDetail performedByUser = userMap.get(performedBy);

        if (user == null || performedByUser == null) {
            throw new BadRequestException("Failed to fetch user details for notification");
        }

        // Prepare Notification
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setToEmail(user.getEmail());
        emailRequest.setSubject("You’ve been added to a project");
        emailRequest.setTemplateCode("project-member-added.html");

        Map<String, Object> variables = new HashMap<>();
        variables.put("memberName", user.getName());
        variables.put("projectName", project.getName());
        variables.put("addedByName", performedByUser.getName());
        variables.put("role", savedProjectMember.getRole());
        // 5. Inject this value instead of hardcoding
        variables.put("projectLink", "http://localhost:5173");
        variables.put("productName", "Task Management Team");
        emailRequest.setVariables(variables);

        // 6. Renamed method for clarity
        notificationProducer.sendEvent(emailRequest, RabbitConfig.PROJECT_MEMBER_ADDED_KEY);

        return ProjectMemberResponse.builder()
                .id(savedProjectMember.getId())
                .projectId(savedProjectMember.getProjectId())
                .role(savedProjectMember.getRole())
                .joinedAt(savedProjectMember.getJoinedAt())
                .user(ProjectMemberResponse.UserSummary.builder()
                        .authId(user.getAuthId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .orgRole(user.getRole())
                        .build())
                .build();
    }
    @Override
    @Transactional
    public void removeMember(String projectId, String authId, String performedBy) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId);
        if (project == null){
            throw new NotFoundException("Project not found!");
        }
        ProjectMember actor = projectMemberRepository.findByProjectIdAndAuthId(projectId, performedBy);
        if(actor.getRole() != Role.OWNER){
            throw new BadRequestException("Only owner can remove members");
        }
        if (Objects.equals(authId, performedBy)){
            throw new BadRequestException("You can't remove yourself");
        }
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndAuthId(projectId, authId);
        if(projectMember == null){
            throw new NotFoundException("Member not found!");
        }
        if(projectMember.getRole() == Role.OWNER){
            long owners = projectMemberRepository.countByProjectIdAndRole(projectId, Role.OWNER);
            if(owners <= 1){
                throw new BadRequestException("At least one owner is required!");
            }
        }
        projectMemberRepository.delete(projectMember);
        project.setMemberCount(project.getMemberCount()-1);
        projectRepository.save(project);

    }

    @Override
    @Transactional
    public ProjectMemberResponse updateRole(String projectId, String authId, UpdateMemberRoleRequest req, String performedBy) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndAuthId(projectId,authId);
        if (member == null){
            throw new NotFoundException("Member not found!");
        }
        ProjectMember actor = projectMemberRepository.findByProjectIdAndAuthId(projectId, performedBy);
        if (actor.getRole() != Role.OWNER){
            throw new BadRequestException("Only owner can update role");
        }
       if(member.getRole() == Role.OWNER && Role.valueOf( String.valueOf(req.getRole()).toUpperCase()) != Role.OWNER){
           long owners = projectMemberRepository.countByProjectIdAndRole(projectId, Role.OWNER);
           if (owners <= 1) throw new BadRequestException("Cannot demote last owner");
       }
       member.setRole(Role.valueOf(String.valueOf(req.getRole()).toUpperCase()));
       ProjectMember updatedMember = projectMemberRepository.saveAndFlush(member);
       UserDetail  userDetail = userClient.getUserById(updatedMember.getAuthId());
       return ProjectMemberResponse.builder()
               .id(updatedMember.getId())
               .projectId(updatedMember.getProjectId())
               .role(updatedMember.getRole())
               .joinedAt(updatedMember.getJoinedAt())
               .user(ProjectMemberResponse.UserSummary.builder()
                       .authId(updatedMember.getAuthId())
                       .name(userDetail.getName())
                       .email(userDetail.getEmail())
                       .avatarUrl(userDetail.getAvatarUrl())
                       .orgRole(userDetail.getRole())
                       .build())
               .build();
    }

    @Override
    @Transactional
    public List<ProjectMemberResponse> listMembers(String projectId) {

        List<ProjectMember> memberResponseList = projectMemberRepository.findByProjectId(projectId);
        List<String> authIds = memberResponseList.stream()
                .map(member->member.getAuthId())
                .toList();
        List<UserDetail> users = userClient.getUsersByIds(authIds);
        Map<String, UserDetail> userDetailMap = users.stream()
                .collect(Collectors.toMap(UserDetail::getAuthId, Function.identity()));
        return memberResponseList.stream()
                .map(member -> {
                    // Look up user details
                    UserDetail userDetails = userDetailMap.get(member.getAuthId());

                    return ProjectMemberResponse.builder()
                            .id(member.getId())
                            .projectId(member.getProjectId())
                            .role(member.getRole())
                            .joinedAt(member.getJoinedAt())
                            // Populate the nested UserSummary
                            .user(mapToSummary(userDetails, member.getAuthId()))
                            .build();
                })
                .collect(Collectors.toList());




    }
    private ProjectMemberResponse.UserSummary mapToSummary(UserDetail user, String authId) {
        if (user == null) {
            return ProjectMemberResponse.UserSummary.builder()
                    .authId(authId)
                    .name("Unknown User") // Fallback
                    .email("")
                    .avatarUrl(null)
                    .build();
        }
        return ProjectMemberResponse.UserSummary.builder()
                .authId(user.getAuthId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .orgRole(user.getRole())
                .build();
    }
}
