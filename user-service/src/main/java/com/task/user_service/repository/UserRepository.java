package com.task.user_service.repository;

import com.task.user_service.dto.UserRequest;
import com.task.user_service.model.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserProfile, String> {
    UserProfile findByAuthId(String authId);

    boolean existsByAuthId(String authId);

    UserProfile findByEmail(String email);
}
