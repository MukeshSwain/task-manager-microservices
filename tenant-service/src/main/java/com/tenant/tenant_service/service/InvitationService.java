package com.tenant.tenant_service.service;

import com.tenant.tenant_service.dto.EmailAndName;
import com.tenant.tenant_service.dto.PendingInvitationResponse;
import com.tenant.tenant_service.dto.RoleAndorgId;
import com.tenant.tenant_service.model.InvitationStatus;
import com.tenant.tenant_service.model.OrganizationInvitation;
import com.tenant.tenant_service.repository.OrganizationInvitationRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvitationService {
    private final OrganizationInvitationRepo invitationRepo;
    private final UserService userService;

    public InvitationService(OrganizationInvitationRepo invitationRepo, UserService userService) {
        this.invitationRepo = invitationRepo;
        this.userService = userService;
    }
    public List<PendingInvitationResponse> getInvitationByOrgId(String orgId) {
        List<OrganizationInvitation> invitations =
                invitationRepo.findAllByOrgIdAndStatus(orgId, InvitationStatus.PENDING);

        return invitations.stream()
                .map(invitation -> {
                    EmailAndName emailAndName = userService.getEmailById(invitation.getInvitedBy());

                    return PendingInvitationResponse.builder()
                            .invitedBy(emailAndName.getName())
                            .role(invitation.getRole().name())
                            .email(invitation.getEmail())
                            .status(invitation.getStatus().name())
                            .build();
                })
                .toList();
    }

}
