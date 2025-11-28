package com.task.user_service.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAndName {
    private String email;
    private String name;
}
