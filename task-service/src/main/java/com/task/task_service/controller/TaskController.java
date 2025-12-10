package com.task.task_service.controller;

import com.task.task_service.dto.CreateTaskRequest;
import com.task.task_service.dto.TaskResponse;
import com.task.task_service.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{projectId}/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/{authId}")
    public ResponseEntity<TaskResponse> createTask(@PathVariable String projectId, @PathVariable String authId,@Valid @RequestBody CreateTaskRequest task) {
        return new ResponseEntity<>(taskService.createTask(task, projectId,authId), HttpStatus.CREATED);
    }
}
