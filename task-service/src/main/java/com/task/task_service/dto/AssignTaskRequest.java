package com.task.task_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTaskRequest {

    @NotNull
    private String assignedToAuthId;
}
