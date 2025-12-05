package com.project.project_service.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "tenant-service", url = "http://localhost:8083/api")
public interface TenantClient {

}
