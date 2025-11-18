package com.tenant.tenant_service.dto;

import com.tenant.tenant_service.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddMemberRequest {


    @NotNull
    private Role role;
    @Email(message = "Email is not valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;
    @NotNull
    private String performedBy;
}
