package com.task.user_service.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UserProfileUpdateRequest {
    private String name;
    private String bio;
    private Map<String, Boolean> notificationPref;

}
