package com.task.user_service.controller;

import com.task.user_service.dto.*;
import com.task.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/{authId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String authId){
        return ResponseEntity.ok(userService.getUserProfile(authId));
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
    public List<UserResponse> getUsersBatch(@RequestBody BatchUserRequest request) {
        return userService.getUsersByIds(request.getUserIds());
    }


}
