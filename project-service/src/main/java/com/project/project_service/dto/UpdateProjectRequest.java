package com.project.project_service.dto;

import com.project.project_service.model.Priority;
import com.project.project_service.model.Status;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProjectRequest {
    private String name;
    private String description;
    private String priority;
    private String status;
    private OffsetDateTime deadline;
    private String teamLeadAuthId;
}
