package com.tenant.tenant_service.controller;

import com.tenant.tenant_service.dto.InvitatationAcceptRequest;
import com.tenant.tenant_service.dto.TokenValidateResponse;
import com.tenant.tenant_service.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
