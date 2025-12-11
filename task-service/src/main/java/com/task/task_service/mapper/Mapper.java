package com.task.task_service.mapper;

import com.task.task_service.dto.TaskResponse;
import com.task.task_service.model.Task;
public class Mapper {
    public static TaskResponse toTaskresponse(Task task){
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(String.valueOf(task.getStatus()))
                .priority(String.valueOf(task.getPriority()))
                .dueDate(task.getDueDate())
                .createdByAuthId(task.getCreatedByAuthId())
                .assignedToAuthId(task.getAssignedToAuthId())
                .tags(task.getTags())
                .projectId(task.getProjectId())
                .attributes(task.getAttributes())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .parentId(task.getProjectId())
                .build();

    }
}
