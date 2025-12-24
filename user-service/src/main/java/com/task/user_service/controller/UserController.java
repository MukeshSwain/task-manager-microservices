package com.task.user_service.controller;

import com.task.user_service.dto.*;
import com.task.user_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserRequest user){
        return ResponseEntity.ok(userService.createUser(user));
    }
    @PutMapping("/{authId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String authId, @RequestBody UserProfileUpdateRequest request){
        return ResponseEntity.ok(userService.updateUser(authId, request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUser(){
        log.info("I am hitted......................");
        String authId = getCurrentUserAuthId();
        log.info("authId: {}", authId);
        UserResponse response = userService.getUserProfile(authId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<String> uploadImage(@PathVariable String id, @RequestParam MultipartFile file){
        return ResponseEntity.ok(userService.uploadImage(id, file));
    }

    @GetMapping("/lookup")
    public ResponseEntity<UserLookupResponse> lookupByEmail(@RequestParam String email){
        return ResponseEntity.ok(userService.lookupByEmail(email));
    }
    @GetMapping("/email/{authId}")
    public ResponseEntity<EmailAndName> getEmailById(@PathVariable String authId){
        return ResponseEntity.ok(userService.getEmailById(authId));
    }
    @PostMapping("/betch-fetch")
    public List<UserResponse> getUsersBatch(@RequestBody List<String> authIds) {
        return userService.getUsersByIds(authIds);
    }
    @GetMapping("/validate/{authId}")
    public ResponseEntity<Boolean> validate(@PathVariable String authId){
        return ResponseEntity.ok(userService.validate(authId));
    }
    private String getCurrentUserAuthId() {
        // The Authentication object was set by your JwtAuthenticationFilter
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // Safety check (though Filter ensures this if configured correctly)
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized access");
        }

        return (String) authentication.getPrincipal();
    }
}
