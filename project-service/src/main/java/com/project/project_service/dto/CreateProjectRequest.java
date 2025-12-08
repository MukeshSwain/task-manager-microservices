package com.project.project_service.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProjectRequest {
    @NotNull
    private String orgId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private String ownerAuthId;

    @NotNull
    private String teamLeadAuthId;
    private String priority;
    private String status;
    private OffsetDateTime deadline;
}
