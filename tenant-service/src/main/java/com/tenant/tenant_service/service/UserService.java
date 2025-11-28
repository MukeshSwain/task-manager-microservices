package com.tenant.tenant_service.service;

import com.tenant.tenant_service.client.UserClient;
import com.tenant.tenant_service.dto.EmailAndName;
import com.tenant.tenant_service.dto.UserLookupResponse;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserClient userClient;

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    public UserLookupResponse lookupUserByEmail(String email) {
        return userClient.lookupByEmail(email);
    }

    public EmailAndName getEmailById(String authId){
        return userClient.getEmailById(authId);
    }
}

