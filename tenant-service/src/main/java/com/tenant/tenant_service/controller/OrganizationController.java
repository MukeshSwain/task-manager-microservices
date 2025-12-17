package com.tenant.tenant_service.controller;

import com.tenant.tenant_service.dto.*;
import com.tenant.tenant_service.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> createCompany(@Valid @RequestBody CreateOrganizationRequest request){
        OrganizationResponse response = service.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{orgId}/add")
    public ResponseEntity<AddMemberResultResponse> addMember(@PathVariable String orgId, @RequestBody AddMemberRequest request){
        return ResponseEntity.ok(service.addMember(orgId,request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<RoleAndorgId>> getMyOrganizations(@RequestParam String authId){
        return ResponseEntity.ok(service.getMyOrganizations(authId));
    }
    @GetMapping("/validate/{orgId}")
    public ResponseEntity<Boolean> validate(@PathVariable String orgId){
        return ResponseEntity.ok(service.validate(orgId));
    }

}
