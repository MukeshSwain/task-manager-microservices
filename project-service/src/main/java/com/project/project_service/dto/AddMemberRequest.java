package com.project.project_service.dto;

import com.project.project_service.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddMemberRequest {
    @NotNull
    private String authId;

    @NotNull
    private Role role;
}
