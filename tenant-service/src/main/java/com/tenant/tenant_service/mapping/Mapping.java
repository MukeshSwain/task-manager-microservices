package com.tenant.tenant_service.mapping;

import com.tenant.tenant_service.dto.MemberResponse;
import com.tenant.tenant_service.dto.OrganizationResponse;
import com.tenant.tenant_service.model.Organization;
import com.tenant.tenant_service.model.OrganizationMember;

public class Mapping {
    public static OrganizationResponse toOrganizationResponse(Organization org){
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .ownerAuthId(org.getOwnerAuthId())
                .domain(org.getDomain())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }

    public static MemberResponse toMemberResponse(OrganizationMember member){
        return MemberResponse.builder()
                .id(member.getId())
                .authId(member.getAuthId())
                .orgId(member.getOrgId())
                .role(String.valueOf(member.getRole()))
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
