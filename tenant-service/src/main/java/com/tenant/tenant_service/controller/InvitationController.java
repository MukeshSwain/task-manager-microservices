package com.tenant.tenant_service.controller;

import com.tenant.tenant_service.dto.PendingInvitationResponse;
import com.tenant.tenant_service.service.InvitationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class InvitationController {
    private final InvitationService service;

    public InvitationController(InvitationService service) {
        this.service = service;
    }
    @GetMapping("/{orgId}/pending")
    public List<PendingInvitationResponse> getPendingInvitations(@PathVariable String orgId){
        return service.getInvitationByOrgId(orgId);
    }

    @PutMapping("/{orgId}/{email}")
    public String cancelInvitation(@PathVariable String orgId, @PathVariable String email){
        return service.cancelInvitation(orgId, email);
    }
}
