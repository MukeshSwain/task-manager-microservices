package com.task.task_service.model;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_tasks_project", columnList = "tenant_id, project_id"),
                @Index(name = "idx_tasks_assigned", columnList = "assigned_to_auth_id"),
                @Index(name = "idx_tasks_parent", columnList = "parent_id"),
                @Index(name = "idx_tasks_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Project (from Project Service)
    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String status; // TODO, IN_PROGRESS, DONE

    @Column(nullable = false)
    private String priority; // LOW, MEDIUM, HIGH

    private OffsetDateTime dueDate;

    @Column(name = "created_by_auth_id", nullable = false)
    private String createdByAuthId;

    @Column(name = "assigned_to_auth_id")
    private String assignedToAuthId;

    // Self-referencing hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Task parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> subTasks = new ArrayList<>();

    // Tags array
    @Type(ListArrayType.class)
    @Column(columnDefinition = "text[]")
    private List<String> tags = new ArrayList<>();

    // Custom JSON attributes
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes = new HashMap<>();

    @Version
    private Integer version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    // Helper methods
    public void addSubTask(Task subTask) {
        subTasks.add(subTask);
        subTask.setParent(this);
    }
}
