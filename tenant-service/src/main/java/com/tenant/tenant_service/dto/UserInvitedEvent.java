package com.tenant.tenant_service.dto;

import com.tenant.tenant_service.model.Role;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInvitedEvent {
    private String type;            // "USER_INVITED"
    private String email;
    private String orgId;
    private Role role;              // OWNER / ADMIN / MEMBER
    private String inviteToken;

}
