package io.github.mostafanasiri.pansy.features.post.domain.model;

import java.util.List;

public record Post(
        Integer id,
        User user,
        String caption,
        List<Image> images,
        Integer likesCount,
        Boolean isLikedByCurrentUser
) {
    public Post(String caption, List<Image> images) {
        this(null, null, caption, images, null, null);
    }

    public Post(Integer id, String caption, List<Image> images) {
        this(id, null, caption, images, null, null);
    }
}
