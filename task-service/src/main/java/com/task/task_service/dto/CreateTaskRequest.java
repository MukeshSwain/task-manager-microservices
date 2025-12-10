package com.task.task_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTaskRequest {
    private String parentId;
    @NotNull
    private String title;
    private String description;
    private String priority;
    private OffsetDateTime dueDate;
    private String assignedToAuthId;

    private List<String> tags;

    private Map<String, Object> attributes;

}
