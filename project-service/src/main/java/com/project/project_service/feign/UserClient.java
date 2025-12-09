package com.project.project_service.feign;

import com.project.project_service.dto.UserDetail;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", url = "http://localhost:8082/api/users")
public interface UserClient {
    @PostMapping("/betch-fetch")
    List<UserDetail> getUsersByIds(@RequestBody List<String> authIds);

    @GetMapping("/{authId}")
    UserDetail getUserById(@NotNull @PathVariable String authId);
}
