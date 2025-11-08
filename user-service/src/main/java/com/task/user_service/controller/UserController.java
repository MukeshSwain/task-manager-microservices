package com.task.user_service.controller;

import com.task.user_service.dto.UserProfileUpdateRequest;
import com.task.user_service.dto.UserRequest;
import com.task.user_service.dto.UserResponse;
import com.task.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<String> updateUser(@PathVariable String authId, @RequestBody UserProfileUpdateRequest request){
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
}
