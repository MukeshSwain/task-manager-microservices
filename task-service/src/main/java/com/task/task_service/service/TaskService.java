package com.task.task_service.service;

import com.task.task_service.dto.*;

import java.util.List;


public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request, String projectId, String authId);
    TaskResponse updateTask(String taskId, UpdateTaskRequest request);
    void assignTask(String taskId, AssignTaskRequest request);
    void changeTaskStatus(String taskId, ChangeTaskStatusRequest request);
    TaskResponse getTaskById(String taskId);
    List<TaskListResponse> getTasksByProject(String projectId);
}
