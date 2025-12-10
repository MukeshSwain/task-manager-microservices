package com.task.task_service.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TaskListResponse {

    private String id;

    private String projectId;

    private String title;
    private String status;
    private String priority;

    private OffsetDateTime dueDate;

    private String assignedToAuthId;

    private String parentId;
}