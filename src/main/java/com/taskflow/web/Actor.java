package com.taskflow.web;

import com.taskflow.domain.Role;
import org.springframework.security.oauth2.jwt.Jwt;

public record Actor(Long id, Role role) {
    public static Actor from(Jwt jwt) {
        return new Actor(Long.valueOf(jwt.getSubject()), Role.valueOf(jwt.getClaimAsStringList("roles").get(0)));
    }
}
