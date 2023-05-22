package io.github.mostafanasiri.pansy.app.presentation.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Pattern(regexp = "^[a-zA-Z0-9._-]{4,255}$", message = "Invalid username") String username,
        @Size(min = 6, max = 500) String password
) {
    public LoginRequest {
        username = username.trim();
        password = password.trim();
    }
}
