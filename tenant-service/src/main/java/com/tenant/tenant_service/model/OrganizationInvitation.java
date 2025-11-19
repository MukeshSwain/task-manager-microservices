package com.tenant.tenant_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "organization_invitation",
uniqueConstraints = @UniqueConstraint(columnNames = {"org_id", "email"})
)
public class OrganizationInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String orgName;
    private String org_id;
    @Column(nullable = false)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false,name = "invited_by")
    private String invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "invited_at", nullable = false)
    private OffsetDateTime invitedAt;
}
