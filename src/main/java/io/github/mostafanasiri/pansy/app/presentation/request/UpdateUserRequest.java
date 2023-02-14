package io.github.mostafanasiri.pansy.app.presentation.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateUserRequest {
    @Size(min = 1, max = 255)
    private String fullName;

    @Size(max = 300)
    private String bio;

    private Integer avatarFileId;

    public UpdateUserRequest(String fullName, String bio, Integer avatarFileId) {
        this.fullName = fullName.trim();
        this.bio = bio.trim();
        this.avatarFileId = avatarFileId;
    }
}
