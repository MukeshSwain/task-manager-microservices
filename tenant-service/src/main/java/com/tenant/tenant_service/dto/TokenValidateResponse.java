package com.tenant.tenant_service.dto;

import com.tenant.tenant_service.model.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenValidateResponse {
private Boolean valid;
private String email;
private Role role;
private String orgId;

}
