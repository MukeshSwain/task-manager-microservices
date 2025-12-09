package com.project.project_service.service.impl;

import com.project.project_service.dto.AddMemberRequest;
import com.project.project_service.dto.ProjectMemberResponse;
import com.project.project_service.dto.UpdateMemberRoleRequest;
import com.project.project_service.dto.UserDetail;
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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        project.setMemberCount(project.getMemberCount()+1);
        projectRepository.save(project);
        UserDetail user = userClient.getUserById(request.getAuthId());

        //Todo : notification

        return ProjectMemberResponse.builder()
                .id(savedProjectMember.getId())
                .projectId(savedProjectMember.getProjectId())
                .role(savedProjectMember.getRole())
                .joinedAt(savedProjectMember.getJoinedAt())
                .user(ProjectMemberResponse.UserSummary.builder()
                        .authId(savedProjectMember.getAuthId())
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
       return Mapping.toProjectMemberResponse(projectMemberRepository.saveAndFlush(member));
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
