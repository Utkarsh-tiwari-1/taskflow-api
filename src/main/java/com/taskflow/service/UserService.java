package com.taskflow.service;

import com.taskflow.domain.AppUser;
import com.taskflow.dto.AuthDtos.UserResponse;
import com.taskflow.dto.UserDtos.UpdateProfileRequest;
import com.taskflow.repository.UserRepository;
import com.taskflow.web.ApiExceptions.NotFound;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) { this.users = users; }

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) { return AuthService.toResponse(findUser(userId)); }

    @Transactional
    public UserResponse updateMe(Long userId, UpdateProfileRequest request) {
        AppUser user = findUser(userId);
        user.updateDisplayName(request.displayName().trim());
        return AuthService.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return users.findAll().stream().map(AuthService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) { return AuthService.toResponse(findUser(userId)); }

    private AppUser findUser(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFound("User was not found"));
    }
}
