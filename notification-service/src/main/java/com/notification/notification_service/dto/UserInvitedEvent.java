package com.notification.notification_service.dto;

import com.notification.notification_service.model.Role;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInvitedEvent {
    private String type;            // "USER_INVITED"
    private String email;
    private String orgId;
    private Role role;              // OWNER / ADMIN / MEMBER
    private String inviteToken;
    private OffsetDateTime timestamp;
}
