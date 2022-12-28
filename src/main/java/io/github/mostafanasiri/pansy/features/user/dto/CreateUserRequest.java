package io.github.mostafanasiri.pansy.features.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateUserRequest {
    @Size(min = 1, max = 255)
    private String fullName;

    @Pattern(regexp = "^[a-zA-Z0-9._-]{4,255}$", message = "Invalid username")
    private String username;

    @Size(min = 6, max = 500)
    private String password;

    public CreateUserRequest(String fullName, String username, String password) {
        this.fullName = fullName.trim();
        this.username = username.trim();
        this.password = password.trim();
    }
}
