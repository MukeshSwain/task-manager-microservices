package com.project.project_service.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", url = "http://localhost:8082/api")
public interface UserClient {

}
