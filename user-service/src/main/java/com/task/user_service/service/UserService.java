package com.task.user_service.service;

import com.task.user_service.dto.*;
import com.task.user_service.model.Role;
import com.task.user_service.model.UserProfile;
import com.task.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;

    public UserService(CloudinaryService cloudinaryService, UserRepository userRepository) {
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
    }

    public String createUser(UserRequest user){
        UserProfile userProfile = UserProfile.builder()
                .authId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .isEmailVerified(user.getIsEmailVerified())
                .role(Role.valueOf(user.getRole().toUpperCase()))
                .build();

        userRepository.save(userProfile);

        return "User created successfully";

    }

    public UserResponse updateUser(String authId, UserProfileUpdateRequest request){

        if(!userRepository.existsByAuthId(authId)){
            throw new RuntimeException("User not found");
        }
        UserProfile userProfile = userRepository.findByAuthId(authId);

        if(request.getName() != null){
            userProfile.setName(request.getName());
        }
        if(request.getBio() != null){
            userProfile.setBio(request.getBio());
        }

        if(request.getNotificationPref() != null){
            userProfile.setNotificationPref(request.getNotificationPref());
        }
        UserProfile user = userRepository.save(userProfile);

        return toUserResponse(user);
    }
    public UserResponse getUserProfile(String authId){

        UserProfile userProfile = userRepository.findByAuthId(authId);
        if(userProfile == null){
            throw new RuntimeException("User not found");
        }
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
                .isEmailVerified(userProfile.getIsEmailVerified())
                .build();
    }

    public String uploadImage(String authId, MultipartFile file) {
        UserProfile userProfile = userRepository.findByAuthId(authId);
        if (userProfile == null) {
            throw new RuntimeException("User not found");
        }

        try {
            // 🧹 Delete old image (if exists)
            if (userProfile.getAvatarPublicId() != null) {
                System.out.println("Deleting old image");
                cloudinaryService.deleteFile(userProfile.getAvatarPublicId());
            }

            // ☁️ Upload new image
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "user-service/image");
            // 🧩 Update DB with new image info
            userProfile.setAvatarPublicId(uploadResult.get("public_id").toString());
            userProfile.setAvatarUrl(uploadResult.get("secure_url").toString()); // ✅ Prefer secure_url
            userRepository.save(userProfile);

            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    public UserLookupResponse lookupByEmail(String email) {
        UserProfile user = userRepository.findByEmail(email);
        if(user == null){
            return UserLookupResponse.builder().exists(false).authId(null).build();
        }
        return UserLookupResponse.builder()
                .exists(true)
                .authId(user.getAuthId())
                .build();
    }

    public EmailAndName getEmailById(String authId) {
        UserProfile user = userRepository.findByAuthId(authId);
        if(user == null){
            throw new RuntimeException("User not found");
        }
        return EmailAndName.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public List<UserResponse> getUsersByIds(List<String> authIds){
        if (authIds == null || authIds.isEmpty()) {
            return List.of();
        }
        List<UserProfile> userProfileList = userRepository.findByAuthIdIn(authIds);
        if (userProfileList.isEmpty()){
            return List.of();
        }
        return userProfileList.stream()
                .map(user->toUserResponse(user)).toList();
    }
}
