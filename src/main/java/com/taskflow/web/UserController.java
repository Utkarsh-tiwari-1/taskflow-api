package com.taskflow.web;

import com.taskflow.domain.Role;
import com.taskflow.dto.AuthDtos.UserResponse;
import com.taskflow.dto.UserDtos.UpdateProfileRequest;
import com.taskflow.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getMe(Actor.from(jwt).id());
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateMe(Actor.from(jwt).id(), request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> listUsers() { return userService.listUsers(); }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable Long userId) { return userService.getUser(userId); }
}
