package com.tenant.tenant_service.controller;

import com.tenant.tenant_service.dto.InvitatationAcceptRequest;
import com.tenant.tenant_service.dto.MemberResponse;
import com.tenant.tenant_service.dto.TokenValidateResponse;
import com.tenant.tenant_service.dto.UpdateRoleRequest;
import com.tenant.tenant_service.model.OrganizationMember;
import com.tenant.tenant_service.model.Role;
import com.tenant.tenant_service.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173",
allowCredentials = "true")
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @GetMapping("/invitation/validate")
    public ResponseEntity<TokenValidateResponse> validateToken(@RequestParam String token){
        return ResponseEntity.ok(service.validateToken(token));
    }

    @PostMapping("/invitation/accept")
    public ResponseEntity<String> acceptInvitation(@RequestBody InvitatationAcceptRequest request){
        return ResponseEntity.ok(service.acceptInvitation(request));
    }

    @PutMapping("/{orgId}/update/role")
    public ResponseEntity<String> updateRole(@PathVariable String orgId,@RequestBody UpdateRoleRequest request){
        System.out.println("jnfjnerjgne : "+orgId);
        return ResponseEntity.ok(service.updateRole(orgId,request));
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable String orgId){
        return ResponseEntity.ok(service.getMembers(orgId));
    }

    @DeleteMapping("/{orgId}/remove/{authId}")
    public ResponseEntity<String> removeMember(@PathVariable String orgId, @PathVariable String authId){
        return ResponseEntity.ok(service.removeMember(orgId, authId));
    }
}
