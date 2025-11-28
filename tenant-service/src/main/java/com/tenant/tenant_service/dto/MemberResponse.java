package com.tenant.tenant_service.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponse {
    private String id;
    private String orgId;
    private String authId;
    private String email;
    private String name;
    private String role;
    OffsetDateTime joinedAt;

}
