package com.tenant.tenant_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationResponse {
    private String id;
    private String name;
    private String ownerAuthId;
    private String domain;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
