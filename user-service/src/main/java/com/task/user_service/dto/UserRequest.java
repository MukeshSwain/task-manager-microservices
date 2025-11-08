package com.task.user_service.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String name;
    @Email(message = "Email is not valid")
    private String email;
    private String role;
    private String id;
}
