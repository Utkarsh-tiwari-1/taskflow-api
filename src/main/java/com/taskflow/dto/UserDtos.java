package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UserDtos {
    private UserDtos() { }

    public record UpdateProfileRequest(@NotBlank @Size(max = 100) String displayName) { }
}
