package com.task.task_service.controller;

import com.task.task_service.dto.TaskResponse;
import com.task.task_service.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173",
        allowCredentials = "true")
@RestController
@RequestMapping("/api/tasks")
public class TasksController {
    private final TaskService taskService;

    public TasksController(TaskService taskService) {
        this.taskService = taskService;
    }
    @PostMapping()
    public ResponseEntity<Page<TaskResponse>> getTasksByOrg(@RequestBody List<String> projectIds,
                                                            @PageableDefault(size = 20,page = 0) Pageable pageable){
        return ResponseEntity.ok(taskService.getTasksByOrg(projectIds,pageable));
    }
}
