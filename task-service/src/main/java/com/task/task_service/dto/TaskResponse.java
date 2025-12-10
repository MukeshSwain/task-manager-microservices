package com.task.task_service.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {
    private String id;
    private String projectId;

    private String title;
    private String description;
    private String status;
    private String priority;

    private OffsetDateTime dueDate;

    private String createdByAuthId;
    private String assignedToAuthId;

    private List<String> tags;
    private Map<String, Object> attributes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private String parentId; // For subtasks

    private List<TaskResponse> subTasks; // Nest
}
