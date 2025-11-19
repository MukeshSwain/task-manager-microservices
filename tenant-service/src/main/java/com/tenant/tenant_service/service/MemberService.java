package com.tenant.tenant_service.service;

import com.tenant.tenant_service.dto.TokenValidateResponse;
import com.tenant.tenant_service.model.OrganizationInvitation;
import com.tenant.tenant_service.repository.OrganizationInvitationRepo;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final OrganizationInvitationRepo invitationRepo;

    public MemberService(OrganizationInvitationRepo invitationRepo) {
        this.invitationRepo = invitationRepo;
    }

    public TokenValidateResponse validateToken(String token){
        OrganizationInvitation organizationInvitation = invitationRepo.findByToken(token);
        if (organizationInvitation != null){
            return TokenValidateResponse.builder()
                    .valid(true)
                    .email(organizationInvitation.getEmail())
                    .role(organizationInvitation.getRole())
                    .orgId(organizationInvitation.getOrg_id())
                    .build();
        }

        return TokenValidateResponse.builder()
                .valid(false)
                .build();
    }
}
