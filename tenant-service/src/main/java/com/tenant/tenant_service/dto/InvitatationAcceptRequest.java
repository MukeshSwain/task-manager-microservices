package com.tenant.tenant_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvitatationAcceptRequest {
    private String token;
    private String authId;
}
