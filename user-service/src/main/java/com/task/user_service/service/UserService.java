package com.task.user_service.service;

import com.task.user_service.dto.UserProfileUpdateRequest;
import com.task.user_service.dto.UserResponse;
import com.task.user_service.model.Role;
import com.task.user_service.model.UserProfile;
import com.task.user_service.dto.UserRequest;
import com.task.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String createUser(UserRequest user){
        UserProfile userProfile = UserProfile.builder()
                .authId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(Role.valueOf(user.getRole().toUpperCase()))
                .build();

        userRepository.save(userProfile);

        return "User created successfully";

    }

    public String updateUser(String authId, UserProfileUpdateRequest request){

        if(!userRepository.existsByAuthId(authId)){
            throw new RuntimeException("User not found");
        }
        UserProfile userProfile = userRepository.findByAuthId(authId);
        System.out.println(userProfile);
        if(request.getName() != null){
            userProfile.setName(request.getName());
        }
        if(request.getBio() != null){
            userProfile.setBio(request.getBio());
        }
        if(request.getAvatarUrl() != null){
            userProfile.setAvatarUrl(request.getAvatarUrl());
        }
        if(request.getNotificationPref() != null){
            userProfile.setNotificationPref(request.getNotificationPref());
        }
        userRepository.save(userProfile);

        return "User updated successfully";
    }
    public UserResponse getUserProfile(String authId){
        if(!userRepository.existsByAuthId(authId)){
            throw new RuntimeException("User not found");
        }
        UserProfile userProfile = userRepository.findByAuthId(authId);
       return toUserResponse(userProfile);

    }

    public List<UserResponse> getAllUsers() {
        List<UserProfile> userProfileList = userRepository.findAll();
        return userProfileList.stream()
                .map(user->toUserResponse(user)).toList();
    }

    private UserResponse toUserResponse(UserProfile userProfile){
        return UserResponse.builder()
                .id(userProfile.getId())
                .role(userProfile.getRole())
                .authId(userProfile.getAuthId())
                .email(userProfile.getEmail())
                .name(userProfile.getName())
                .bio(userProfile.getBio())
                .avatarUrl(userProfile.getAvatarUrl())
                .notificationPref(userProfile.getNotificationPref())
                .createdAt(userProfile.getCreatedAt())
                .build();
    }
}
