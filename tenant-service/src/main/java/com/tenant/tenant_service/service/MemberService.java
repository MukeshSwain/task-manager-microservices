package com.tenant.tenant_service.service;

import com.tenant.tenant_service.config.RabbitConfig;
import com.tenant.tenant_service.dto.*;
import com.tenant.tenant_service.exception.BadRequestException;
import com.tenant.tenant_service.exception.NotFoundException;
import com.tenant.tenant_service.messaging.NotificationProducer;
import com.tenant.tenant_service.model.InvitationStatus;
import com.tenant.tenant_service.model.OrganizationInvitation;
import com.tenant.tenant_service.model.OrganizationMember;
import com.tenant.tenant_service.model.Role;
import com.tenant.tenant_service.repository.OrganizationInvitationRepo;
import com.tenant.tenant_service.repository.OrganizationMemberRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static com.tenant.tenant_service.mapping.Mapping.toMemberResponse;


import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
public class MemberService {
    private final OrganizationInvitationRepo invitationRepo;
    private final OrganizationMemberRepo memberRepo;
    private final UserService userService;
    private final NotificationProducer notificationProducer;
    private final OrganizationService organizationService;

    public MemberService(OrganizationInvitationRepo invitationRepo, OrganizationMemberRepo memberRepo, UserService userService, NotificationProducer notificationProducer, OrganizationService organizationService) {
        this.invitationRepo = invitationRepo;
        this.memberRepo = memberRepo;
        this.userService = userService;
        this.notificationProducer = notificationProducer;
        this.organizationService = organizationService;
    }

    public TokenValidateResponse validateToken(String token){
        OrganizationInvitation organizationInvitation = invitationRepo.findByToken(token);
        if (organizationInvitation == null)
        {
            throw new BadRequestException("Invalid invitation");
        }
        if (organizationInvitation.getStatus() != InvitationStatus.PENDING)
        {
            throw new BadRequestException("Invalid token");
        }
        return TokenValidateResponse.builder()
                .valid(true)
                .email(organizationInvitation.getEmail())
                .role(organizationInvitation.getRole())
                .orgId(organizationInvitation.getOrgId())
                .orgName(organizationInvitation.getOrgName())
                .build();


    }

    public String acceptInvitation(InvitatationAcceptRequest request){
        OrganizationInvitation organizationInvitation = invitationRepo.findByToken(request.getToken());
        if (organizationInvitation == null){
            throw new RuntimeException("Invalid token");
        }

        //first update status in invitation table
        organizationInvitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepo.save(organizationInvitation);

        //then add member in member table
        OrganizationMember organizationMember = OrganizationMember.builder()
                        .orgId(organizationInvitation.getOrgId())
                        .role(organizationInvitation.getRole())
                        .authId(request.getAuthId())
                        .joinedAt(OffsetDateTime.now())
                        .build();
        String orgName = organizationService.getOrgNameById(organizationMember.getOrgId());
        memberRepo.save(organizationMember);
        notificationProducer.send(EmailEvent.builder()
                .email(organizationInvitation.getEmail())
                .subject("You have been added to an organization")
                .message("You have been added to an organization " + orgName + " as " + organizationInvitation.getRole())
                .build(), RabbitConfig.MEMBER_ADDED_KEY);
        return "Invitation accepted successfully";

    }

    public String updateRole(String orgId, UpdateRoleRequest request) {
        OrganizationMember organizationMember = memberRepo.findByOrgIdAndAuthId(orgId, request.getAuthId());
        if(organizationMember == null){
            throw new NotFoundException("Member not found!");
        }
        if(organizationMember.getRole().equals(Role.OWNER)){
            throw new BadRequestException("Owner cannot be updated");
        }
        organizationMember.setRole(Role.valueOf(request.getRole().toUpperCase()));
        String orgName = organizationService.getOrgNameById(orgId);
        notificationProducer.send(EmailEvent.builder()
                .email(userService.getEmailById(request.getAuthId()).getEmail())
                        .subject("Your role has been updated")
                        .message("Your role has been updated to " + request.getRole()+" in "+orgName)
                .build(),
                RabbitConfig.ROLE_UPDATED_KEY);
        memberRepo.save(organizationMember);
        return "Role updated successfully";

    }

    public List<MemberResponse> getMembers(String orgId) {
        List<OrganizationMember> membersList = memberRepo.findByOrgId(orgId);

        if (membersList.isEmpty()) {
            throw new NotFoundException("Members not found!");
        }

        return membersList.stream()
                .map(member -> {
                    // Call user service to get user info
                    EmailAndName user = userService.getEmailById(member.getAuthId());

                    return MemberResponse.builder()
                            .id(member.getId())
                            .orgId(member.getOrgId())
                            .authId(member.getAuthId())
                            .email(user.getEmail())    // from user service
                            .name(user.getName())      // from user service
                            .role(member.getRole().name())  // from member table
                            .joinedAt(member.getJoinedAt())
                            .build();
                })
                .toList();
    }


    public String removeMember(String orgId, String authId) {
        OrganizationMember organizationMember = memberRepo.findByOrgIdAndAuthId(orgId, authId);
        if (organizationMember.getRole().equals(Role.OWNER)) {
            throw new BadRequestException("Owner cannot be removed");
        }

        if(organizationMember == null) {
            throw new NotFoundException("Member not found!");
        }
        String email = userService.getEmailById(authId).getEmail();
        String orgName = organizationService.getOrgNameById(organizationMember.getOrgId());
        notificationProducer.send(EmailEvent.builder()
                .email(email)
                        .subject("You were removed from the organization")
                        .message("You are no longer a member of "+orgName)
                .build(), RabbitConfig.MEMBER_REMOVED_KEY);
        memberRepo.delete(organizationMember);
        return "Member removed successfully";
    }

    public MemberResponse getMember(String orgId, String authId) {
        OrganizationMember member = memberRepo.findByOrgIdAndAuthId(orgId, authId);
        if(member == null){
            throw new NotFoundException("Member not found");
        }
        EmailAndName emailAndName = userService.getEmailById(authId);
        return MemberResponse.builder()
                .name(emailAndName.getName())
                .email(emailAndName.getEmail())
                .role(member.getRole().name())
                .authId(authId)
                .orgId(orgId)
                .joinedAt(member.getJoinedAt())
                .id(member.getId())
                .build();
    }
}
