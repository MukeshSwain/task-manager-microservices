package com.task.task_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeTaskStatusRequest {

    @NotBlank
    private String status;
}