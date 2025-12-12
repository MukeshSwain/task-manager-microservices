package com.task.task_service.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;


@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private String priority;
    private OffsetDateTime dueDate;
    private List<String> tags;
    private Map<String, Object> attributes;
}