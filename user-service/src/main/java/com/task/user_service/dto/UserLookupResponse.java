package com.task.user_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLookupResponse {
    private boolean exists;
    private String authId;
}
