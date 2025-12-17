package com.project.project_service.repository;

import com.project.project_service.model.Project;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Project findByIdAndDeletedFalse(String projectId);

    List<Project> findAllByOrgIdAndDeletedFalse(String orgId);

    List<Project> findAllByIdInAndDeletedFalse(List<String> projectIds);

    @Query("SELECT p.id FROM Project p WHERE p.orgId = :orgId AND p.deleted = false")
    List<String> findIdsByOrgIdAndDeletedFalse(@Param("orgId") String orgId);
}
