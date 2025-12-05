package com.project.project_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "project_members",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "auth_id"})},
        indexes = {@Index(name = "idx_pm_project", columnList = "project_id"), @Index(name = "idx_pm_auth", columnList = "auth_id")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "project_id",nullable = false)
    private String projectId;

    @Column(name = "auth_id",nullable = false)
    private String authId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name="joined_at",nullable = false)
    private OffsetDateTime joinedAt;

}
