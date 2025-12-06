package com.project.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponse {
    private String id;
    private String orgId;
    private String authId;
    private String email;
    private String name;
    private String role;
    OffsetDateTime joinedAt;
}
