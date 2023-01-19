package io.github.mostafanasiri.pansy.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Size(min = 1, max = 255) String fullName,
        @Pattern(regexp = "^[a-zA-Z0-9._-]{4,255}$", message = "Invalid username") String username,
        @Size(min = 6, max = 500) String password
) {
    public RegisterRequest {
        fullName = fullName.trim();
        username = username.trim();
        password = password.trim();
    }
}
