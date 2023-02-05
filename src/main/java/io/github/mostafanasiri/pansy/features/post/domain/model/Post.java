package io.github.mostafanasiri.pansy.features.post.domain.model;

import io.github.mostafanasiri.pansy.features.user.domain.model.User;

import java.util.Date;
import java.util.List;

public record Post(
        Integer id,
        User user,
        String caption,
        List<Image> images,
        Integer likeCount,
        Integer commentCount,
        Boolean isLikedByAuthenticatedUser,
        Date createdAt
) {
    public Post(String caption, List<Image> images) {
        this(null, null, caption, images, null, null, null, null);
    }

    public Post(Integer id, String caption, List<Image> images) {
        this(id, null, caption, images, null, null, null, null);
    }
}
