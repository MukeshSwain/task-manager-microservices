package com.task.task_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://localhost:8082/api/users")
public interface UserClient {
    @GetMapping("/validate/{authId}")
    Boolean getUserById(@PathVariable String authId);
}
