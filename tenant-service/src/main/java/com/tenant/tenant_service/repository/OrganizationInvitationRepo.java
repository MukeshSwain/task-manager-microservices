package com.tenant.tenant_service.repository;

import com.tenant.tenant_service.model.InvitationStatus;
import com.tenant.tenant_service.model.OrganizationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationInvitationRepo extends JpaRepository<OrganizationInvitation,String> {
    OrganizationInvitation findByToken(String token);

    void deleteByToken(String token);
    List<OrganizationInvitation> findAllByOrgIdAndStatus(String orgId, InvitationStatus invitationStatus);
}
