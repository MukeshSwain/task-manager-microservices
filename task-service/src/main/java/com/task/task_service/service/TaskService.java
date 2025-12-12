package com.task.task_service.service;

import com.task.task_service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;


public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request, String projectId, String authId);
    TaskResponse updateTask(String taskId, UpdateTaskRequest request);
    TaskResponse assignTask(String taskId, AssignTaskRequest request);
    TaskResponse changeTaskStatus(String taskId, ChangeTaskStatusRequest request);
    TaskResponse getTaskById(String taskId);
    @Transactional(readOnly = true)
    Page<TaskListResponse> getTasksByProject(String projectId, int page, int size);
    Void deleteTask(String taskId);
}
