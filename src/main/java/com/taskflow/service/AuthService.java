package com.taskflow.service;

import com.taskflow.config.JwtProperties;
import com.taskflow.domain.AppUser;
import com.taskflow.domain.Role;
import com.taskflow.dto.AuthDtos.AuthResponse;
import com.taskflow.dto.AuthDtos.LoginRequest;
import com.taskflow.dto.AuthDtos.RegisterRequest;
import com.taskflow.dto.AuthDtos.UserResponse;
import com.taskflow.repository.UserRepository;
import com.taskflow.web.ApiExceptions.Conflict;
import com.taskflow.web.ApiExceptions.Unauthorized;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) throw new Conflict("An account with this email already exists");
        AppUser user = users.save(new AppUser(email, passwordEncoder.encode(request.password()),
                request.displayName().trim(), Role.USER));
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = users.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new Unauthorized("Email or password is incorrect"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new Unauthorized("Email or password is incorrect");
        }
        return issueToken(user);
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResponse issueToken(AppUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("taskflow-api")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", List.of(user.getRole().name()))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", expiresAt, toResponse(user));
    }

    public static UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(), user.getCreatedAt());
    }
}
