package com.taskflow.config;

import com.taskflow.domain.AppUser;
import com.taskflow.domain.Role;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AuthService;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final Environment environment;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(Environment environment, UserRepository users, PasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = environment.getProperty("ADMIN_EMAIL");
        String password = environment.getProperty("ADMIN_PASSWORD");
        if ((email == null || email.isBlank()) && (password == null || password.isBlank())) return;
        if (email == null || email.isBlank() || password == null || password.length() < 12) {
            throw new IllegalStateException("Set both ADMIN_EMAIL and an ADMIN_PASSWORD with at least 12 characters");
        }
        String normalizedEmail = AuthService.normalizeEmail(email);
        if (!users.existsByEmail(normalizedEmail)) {
            users.save(new AppUser(normalizedEmail, passwordEncoder.encode(password), "TaskFlow Admin", Role.ADMIN));
        }
    }
}
