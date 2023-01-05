package io.github.mostafanasiri.pansy.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {
    @Pattern(regexp = "^[a-zA-Z0-9._-]{4,255}$", message = "Invalid username")
    private String username;

    @Size(min = 6, max = 500)
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username.trim();
        this.password = password.trim();
    }
}
