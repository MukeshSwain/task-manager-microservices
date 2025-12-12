package com.task.task_service.repository;

import com.task.task_service.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
    Page<Task> findAllByProjectId(String projectId, Pageable pageable);
}
