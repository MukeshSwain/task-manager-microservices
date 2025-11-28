package com.tenant.tenant_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleAndorgId {
    private String orgId;
    private String role;
    private String orgName;
    private LocalDateTime joinedAt;
}
