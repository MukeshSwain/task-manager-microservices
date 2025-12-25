package com.tenant.tenant_service.client;

import com.tenant.tenant_service.config.FeignClientConfig;
import com.tenant.tenant_service.dto.EmailAndName;
import com.tenant.tenant_service.dto.UserLookupResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8082/api/users",
        configuration = FeignClientConfig.class
)
public interface UserClient {
    @GetMapping("/lookup")
    public UserLookupResponse lookupByEmail(@RequestParam String email);

    @GetMapping("/email/{authId}")
    EmailAndName getEmailById(@PathVariable String authId);
}
