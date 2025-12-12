package com.task.task_service.controller;

import com.task.task_service.dto.*;
import com.task.task_service.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable String taskId, @RequestBody UpdateTaskRequest request){
        return new ResponseEntity<>(taskService.updateTask(taskId,request), HttpStatus.OK);
    }
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String taskId){
        return new ResponseEntity<>(taskService.getTaskById(taskId),HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<TaskListResponse>> getTasksByProject(@PathVariable String projectId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size
                                                                    ){
        return new ResponseEntity<>(taskService.getTasksByProject(projectId,page,size),HttpStatus.OK);
    }
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> changeTaskStatus(@PathVariable String taskId, @RequestBody ChangeTaskStatusRequest request){
        return new ResponseEntity<>(taskService.changeTaskStatus(taskId,request), HttpStatus.OK);
    }
    @PutMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable String taskId, @Valid @RequestBody AssignTaskRequest request){
        return new ResponseEntity<>(taskService.assignTask(taskId,request), HttpStatus.OK);
    }
}
