package com.taskflow.dto;

import com.taskflow.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class AuthDtos {
    private AuthDtos() { }

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 10, max = 72) String password,
            @NotBlank @Size(min = 1, max = 100) String displayName) { }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 72) String password) { }

    public record AuthResponse(String accessToken, String tokenType, Instant expiresAt, UserResponse user) { }

    public record UserResponse(Long id, String email, String displayName, Role role, Instant createdAt) { }
}
