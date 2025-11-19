package com.tenant.tenant_service.dto;

import com.tenant.tenant_service.model.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberResultResponse {
    private String status;        // "MEMBER_ADDED" or "INVITATION_SENT"
    private MemberResponse member; // when member is added
    private String email;         // when invitation is sent
    private Role role;
}
