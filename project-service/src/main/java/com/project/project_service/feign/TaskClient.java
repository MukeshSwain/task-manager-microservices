package com.project.project_service.feign;

import com.project.project_service.dto.TaskResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "task-service", url = "http://localhost:8086")
public interface TaskClient {
    @PostMapping("/api/tasks")
    public Page<TaskResponse> getTasksByOrg(@RequestBody List<String> projectIds, @PageableDefault(size = 20) Pageable pageable);
}
