package com.tenant.tenant_service.repository;

import com.tenant.tenant_service.model.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationMemberRepo extends JpaRepository<OrganizationMember, String> {
    OrganizationMember findByOrgIdAndAuthId(String orgId, String performedBy);

    List<OrganizationMember> findByOrgId(String orgId);

    OrganizationMember findByAuthId(String authId);
}
