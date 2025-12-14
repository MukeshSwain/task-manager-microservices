package com.project.project_service.repository;

import com.project.project_service.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Project findByIdAndDeletedFalse(String projectId);

    List<Project> findAllByOrgIdAndDeletedFalse(String orgId);

    List<Project> findAllByIdInAndDeletedFalse(List<String> projectIds);
}
