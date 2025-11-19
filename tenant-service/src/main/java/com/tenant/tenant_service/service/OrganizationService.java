package com.tenant.tenant_service.service;

import com.tenant.tenant_service.dto.*;
import com.tenant.tenant_service.exception.BadRequestException;
import com.tenant.tenant_service.messaging.NotificationProducer;
import com.tenant.tenant_service.model.*;
import com.tenant.tenant_service.repository.OrganizationInvitationRepo;
import com.tenant.tenant_service.repository.OrganizationMemberRepo;
import com.tenant.tenant_service.repository.OrganizationRepo;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;


@Service
public class OrganizationService {
    private final OrganizationRepo organizationRepo;
    private final OrganizationMemberRepo memberRepo;
    private final UserService userService;
    private final OrganizationInvitationRepo organizationInvitationRepo;
    private final NotificationProducer notificationProducer;

    public OrganizationService(OrganizationRepo organizationRepo, OrganizationMemberRepo memberRepo, UserService userService, OrganizationInvitationRepo organizationInvitationRepo, NotificationProducer notificationProducer) {
        this.organizationRepo = organizationRepo;
        this.memberRepo = memberRepo;
        this.userService = userService;
        this.organizationInvitationRepo = organizationInvitationRepo;
        this.notificationProducer = notificationProducer;
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request){
        Organization existOrg = organizationRepo.findByName(request.getName());
        if(existOrg != null){
            throw new BadRequestException("Organization already exists");
        }
        if (request.getAuthId() == null) {
            throw new BadRequestException("Owner authId is required");
        }
        Organization org = Organization.builder()
                .name(request.getName())
                .ownerAuthId(request.getAuthId())
                .domain(request.getDomain())
                .build();

        Organization savedOrg = organizationRepo.save(org);
        OrganizationMember owner = OrganizationMember.builder()
                .orgId(savedOrg.getId())
                .authId(savedOrg.getOwnerAuthId())
                .role(Role.OWNER)
                .joinedAt(OffsetDateTime.now())
                .build();

        memberRepo.save(owner);
        return toOrganizationResponse(savedOrg);

    }

    public AddMemberResultResponse addMember(String orgId, AddMemberRequest request){
        Organization existOrg = organizationRepo.findById(orgId).orElse(null);
        if(existOrg == null){
            throw new BadRequestException("Organization not found");
        }
        Role actorRole = getRoleByAuthId(orgId, request.getPerformedBy());
        if (actorRole == null ||
                (actorRole != Role.ADMIN &&
                        actorRole != Role.OWNER &&
                        actorRole != Role.MANAGER)) {

            throw new BadRequestException("You are not authorized to add member");
        }

       UserLookupResponse userResponse = userService.lookupUserByEmail(request.getEmail());
        if (!userResponse.exists){
            String token = UUID.randomUUID().toString().replace("-", "");
            OrganizationInvitation organizationInvitation = OrganizationInvitation.builder()
                    .org_id(orgId)
                    .email(request.getEmail())
                    .role(request.getRole())
                    .token(token)
                    .orgName(existOrg.getName())
                    .invitedBy(request.getPerformedBy())
                    .status(InvitationStatus.PENDING)
                    .invitedAt(OffsetDateTime.now())
                    .build();

            organizationInvitationRepo.save(organizationInvitation);
            notificationProducer.sendUserInvitedEvent(UserInvitedEvent.builder()
                    .email(request.getEmail())
                    .role(request.getRole())
                    .inviteToken(token)
                    .orgId(orgId)
                    .type("Invited")
                    .build());
            return AddMemberResultResponse.builder()
                    .status("INVITATION_SENT")
                    .email(request.getEmail())
                    .role(request.getRole())
                    .build();


        }
        String authId = userResponse.authId;
        System.out.println("AuthId : "+authId);
        OrganizationMember existMember = memberRepo.findByOrgIdAndAuthId(orgId, authId);
        System.out.println("ExistMember : "+existMember);
        if(existMember != null){
            throw new BadRequestException("User is already member of organization");
        }
        OrganizationMember newMember = OrganizationMember.builder()
                .authId(authId)
                .orgId(orgId)
                .role(request.getRole())
                .joinedAt(OffsetDateTime.now())
                .build();

        OrganizationMember savedMember = memberRepo.save(newMember);


        return AddMemberResultResponse.builder()
                .status("MEMBER_ADDED")
                .member(toMemberResponse(savedMember))
                .email(request.getEmail())
                .build();

    }

    private Role getRoleByAuthId(String orgId, String performedBy) {

        OrganizationMember member = memberRepo.findByOrgIdAndAuthId(orgId, performedBy);

        if (member == null) {
            System.out.println("Actor is not a member of this organization");
            return null;   // or throw an exception if preferred
        }

        return member.getRole();
    }



    private OrganizationResponse toOrganizationResponse(Organization org){
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .ownerAuthId(org.getOwnerAuthId())
                .domain(org.getDomain())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }

    private MemberResponse toMemberResponse(OrganizationMember member){
        return MemberResponse.builder()
                .id(member.getId())
                .authId(member.getAuthId())
                .orgId(member.getOrgId())
                .role(String.valueOf(member.getRole()))
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
