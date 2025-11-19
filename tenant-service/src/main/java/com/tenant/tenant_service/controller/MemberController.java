package com.tenant.tenant_service.controller;

import com.tenant.tenant_service.dto.TokenValidateResponse;
import com.tenant.tenant_service.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidateResponse> validateToken(@RequestParam String token){
        return ResponseEntity.ok(service.validateToken(token));
    }
}
