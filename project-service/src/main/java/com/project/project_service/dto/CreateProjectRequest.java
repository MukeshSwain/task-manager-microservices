package com.project.project_service.dto;

import com.project.project_service.model.Priority;
import com.project.project_service.model.Status;
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
    private Priority priority;
    private Status status;
    private OffsetDateTime deadline;
}
