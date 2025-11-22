package com.tenant.tenant_service.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoleRequest {
    private String role;
    private String authId;
}
