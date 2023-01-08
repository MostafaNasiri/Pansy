package io.github.mostafanasiri.pansy.features.post.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class CreatePostRequest {
    @Size(min = 1, max = 1000)
    private String caption;

    @NotNull
    @Size(min = 1, max = 3)
    private List<Integer> imageIds;

    public CreatePostRequest(String caption, List<Integer> imageIds) {
        this.caption = caption.trim();
        this.imageIds = imageIds;
    }
}
