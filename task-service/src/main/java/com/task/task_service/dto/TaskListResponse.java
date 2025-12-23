package com.task.task_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class TaskListResponse {

    private String id;

    private String projectId;
    private String description;
    private String title;
    private String status;
    private String priority;

    private OffsetDateTime dueDate;
    private String createdByAuthId;

    private String assignedToAuthId;
    private List<String> tags;
    private Map<String, Object> attributes;
    private String parentId;
}