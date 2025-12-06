package com.project.project_service.feign;

import com.project.project_service.dto.MemberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tenant-service", url = "http://localhost:8083/api/members")
public interface TenantClient {

    @GetMapping("/{orgId}/{authId}/get")
    MemberResponse getMember(
            @PathVariable("orgId") String orgId,
            @PathVariable("authId") String authId
    );
}
