package com.tenant.tenant_service.repository;

import com.tenant.tenant_service.model.OrganizationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationInvitationRepo extends JpaRepository<OrganizationInvitation,String> {
    OrganizationInvitation findByToken(String token);
}
