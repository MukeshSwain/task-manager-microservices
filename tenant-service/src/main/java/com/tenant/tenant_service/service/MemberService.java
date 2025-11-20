package com.tenant.tenant_service.service;

import com.tenant.tenant_service.dto.InvitatationAcceptRequest;
import com.tenant.tenant_service.dto.TokenValidateResponse;
import com.tenant.tenant_service.model.InvitationStatus;
import com.tenant.tenant_service.model.OrganizationInvitation;
import com.tenant.tenant_service.model.OrganizationMember;
import com.tenant.tenant_service.repository.OrganizationInvitationRepo;
import com.tenant.tenant_service.repository.OrganizationMemberRepo;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class MemberService {
    private final OrganizationInvitationRepo invitationRepo;
    private final OrganizationMemberRepo memberRepo;

    public MemberService(OrganizationInvitationRepo invitationRepo, OrganizationMemberRepo memberRepo) {
        this.invitationRepo = invitationRepo;
        this.memberRepo = memberRepo;
    }

    public TokenValidateResponse validateToken(String token){
        OrganizationInvitation organizationInvitation = invitationRepo.findByToken(token);
        if (organizationInvitation != null){
            return TokenValidateResponse.builder()
                    .valid(true)
                    .email(organizationInvitation.getEmail())
                    .role(organizationInvitation.getRole())
                    .orgId(organizationInvitation.getOrg_id())
                    .orgName(organizationInvitation.getOrgName())
                    .build();
        }

        return TokenValidateResponse.builder()
                .valid(false)
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
                        .orgId(organizationInvitation.getOrg_id())
                        .role(organizationInvitation.getRole())
                        .authId(request.getAuthId())
                        .joinedAt(OffsetDateTime.now())
                        .build();
        memberRepo.save(organizationMember);
        return "Invitation accepted successfully";

    }
}
