package com.task.task_service.service.impl;

import com.task.task_service.dto.*;
import com.task.task_service.exception.ResourceNotFoundException; // Assumed Custom Exception
import com.task.task_service.feign.ProjectClient;
import com.task.task_service.feign.UserClient;
import com.task.task_service.mapper.Mapper;
import com.task.task_service.model.Priority;
import com.task.task_service.model.Task;
import com.task.task_service.model.Status; // Assumed Enum
import com.task.task_service.repository.TaskRepository;
import com.task.task_service.service.TaskService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectClient projectClient;
    private final UserClient userClient;
    public TaskServiceImpl(TaskRepository taskRepository, ProjectClient projectClient, UserClient userClient) {
        this.taskRepository = taskRepository;
        this.projectClient = projectClient;
        this.userClient = userClient;
    }

    @Override
    public TaskResponse createTask(CreateTaskRequest request, String projectId, String authId) {
        log.info("Creating task for project: {}", projectId);

        // 1. Validate Project
        boolean isExistProject = projectClient.getProjectById(projectId);
        if(!isExistProject){
            throw new ResourceNotFoundException("Project not found");
        }

        // 2. Validate Assignee (if provided)
        if(request.getAssignedToAuthId() != null){
            boolean isExistUser = userClient.getUserById(request.getAssignedToAuthId());
            if(!isExistUser){
                throw new ResourceNotFoundException("User not found");
            }
        }

        // 3. Handle Parent Task
        Task parent = null;
        if(request.getParentId() != null){
            parent = taskRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Task not found"));
        }

        // 4. Safe Enum Parsing
        Priority priority = parsePriority(request.getPriority());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .projectId(projectId)
                .priority(priority)
                .status(Status.TODO)
                .dueDate(request.getDueDate())
                .createdByAuthId(authId)
                .assignedToAuthId(request.getAssignedToAuthId())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .attributes(request.getAttributes() != null ? request.getAttributes() : new HashMap<>())
                .parent(parent) // Hibernate handles the relationship automatically
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", saved.getId());
        return Mapper.toTaskresponse(saved);
    }

    @Override
    public TaskResponse updateTask(String taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()->new ResourceNotFoundException("Task not found"));
        if (request.getAssignedToAuthId() != null && !request.getAssignedToAuthId().equals(task.getAssignedToAuthId())){
            boolean isExistUser = userClient.getUserById(request.getAssignedToAuthId());
            if(!isExistUser){
                throw new ResourceNotFoundException("User not found");
            }
            task.setAssignedToAuthId(request.getAssignedToAuthId());
        }
        updateIfNotNull(request.getTitle(),task::setTitle);
        updateIfNotNull(request.getDescription(),task::setDescription);
        updateIfNotNull(request.getDueDate(),task::setDueDate);
        updateIfNotNull(request.getTags(),task::setTags);
        updateIfNotNull(request.getAttributes(),task::setAttributes);

        if (request.getPriority() != null){
            task.setPriority(parsePriority(request.getPriority()));
        }
        if (request.getStatus() !=null){
            task.setStatus(parseStatus(request.getStatus()));
        }
        Task updated = taskRepository.save(task);
       return Mapper.toTaskresponse(updated);
    }

    @Override
    @Transactional
    public void assignTask(String taskId, AssignTaskRequest request) {

    }

    @Override
    @Transactional
    public void changeTaskStatus(String taskId, ChangeTaskStatusRequest request) {

    }

    @Override
    public TaskResponse getTaskById(String taskId) {
       return null;
    }

    @Override
    public List<TaskListResponse> getTasksByProject(String projectId) {
        return null;
    }

    private Priority parsePriority(String priorityStr) {
        if (priorityStr == null) return Priority.MEDIUM;
        try {
            return Priority.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid priority {}, defaulting to MEDIUM", priorityStr);
            return Priority.MEDIUM;
        }
    }
    private Status parseStatus(String statusStr){
        if (statusStr == null){
            return Status.TODO;
        }
        try {
            return Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status {}, defaulting to TODO", statusStr);
            return Status.TODO;
        }
    }
    private <T> void updateIfNotNull(T value, Consumer<T> setter){
        if(value != null){
            setter.accept(value);
        }
    }
}