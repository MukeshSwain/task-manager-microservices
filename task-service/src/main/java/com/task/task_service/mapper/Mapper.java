package com.task.task_service.mapper;

import com.task.task_service.dto.TaskListResponse;
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

    public static TaskListResponse toTaskListResponse(Task task) {
        return TaskListResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .createdByAuthId(task.getCreatedByAuthId())
                .description(task.getDescription())
                .assignedToAuthId(task.getAssignedToAuthId())
                .projectId(task.getProjectId())
                .parentId(task.getParent()!=null ?task.getParent().getId():null)
                .tags(task.getTags())
                .attributes(task.getAttributes())
                .build();
    }
}
