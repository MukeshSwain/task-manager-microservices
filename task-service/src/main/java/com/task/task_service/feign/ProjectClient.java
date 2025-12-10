package com.task.task_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "project-service", url = "http://localhost:8085/api/projects")
public interface ProjectClient {
    @GetMapping("/validate/{projectId}")
    Boolean getProjectById(@PathVariable String projectId);
}
