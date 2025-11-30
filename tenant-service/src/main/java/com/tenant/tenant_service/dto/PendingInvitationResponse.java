package com.tenant.tenant_service.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingInvitationResponse {
    private String invitedBy;
    private String email;
    private String role;
    private String status;
    private OffsetDateTime invitedAt;
}
