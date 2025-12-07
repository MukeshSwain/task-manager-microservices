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
public class ProjectResponse {
    private String id;
    private String orgId;
    private String ownerAuthId;
    private String name;
    private String description;
    private String teamLeadAuthId;
    private Priority priority;
    private Status status;
    private Integer memberCount;
    private OffsetDateTime deadline;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
