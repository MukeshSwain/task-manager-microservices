package com.tenant.tenant_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrganizationRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "AuthId cannot be null")
    private String authId;
    private String domain;
}
