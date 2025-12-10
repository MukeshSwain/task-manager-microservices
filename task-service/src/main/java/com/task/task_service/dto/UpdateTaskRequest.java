package com.task.task_service.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;


@Data
public class UpdateTaskRequest {

    private String title;

    private String description;

    private String status; // TODO, IN_PROGRESS, DONE

    private String priority;

    private OffsetDateTime dueDate;

    private String assignedToAuthId;

    private List<String> tags;

    private Map<String, Object> attributes;
}