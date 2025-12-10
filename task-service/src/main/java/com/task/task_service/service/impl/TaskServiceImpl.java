package com.task.task_service.service.impl;

import com.task.task_service.dto.*;
import com.task.task_service.feign.ProjectClient;
import com.task.task_service.repository.TaskRepository;
import com.task.task_service.service.TaskService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ProjectClient projectClient;

    public TaskServiceImpl(TaskRepository taskRepository, ProjectClient projectClient) {
        this.taskRepository = taskRepository;
        this.projectClient = projectClient;
    }

    @Override
    public TaskResponse createTask(CreateTaskRequest request, String projectId, String authId) {
        Boolean isExistProject = projectClient.getProjectById(projectId);
        if(isExistProject == null){
            throw new RuntimeException("Project not found");
        }
        if(request.getAssignedToAuthId() != null){

        }

        return null;
    }

    @Override
    public TaskResponse updateTask(String taskId, UpdateTaskRequest request) {
        return null;
    }

    @Override
    public void assignTask(String taskId, AssignTaskRequest request) {

    }

    @Override
    public void changeTaskStatus(String taskId, ChangeTaskStatusRequest request) {

    }

    @Override
    public TaskResponse getTaskById(String taskId) {
        return null;
    }

    @Override
    public List<TaskListResponse> getTasksByProject(String projectId) {
        return List.of();
    }
}
