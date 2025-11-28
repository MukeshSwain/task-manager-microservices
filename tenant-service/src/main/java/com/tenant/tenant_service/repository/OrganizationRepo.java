package com.tenant.tenant_service.repository;

import com.tenant.tenant_service.model.Organization;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepo extends JpaRepository<Organization, String> {

    Organization findByName(@NotBlank String name);

    List<Organization> findByOwnerAuthId(String authId);


}
