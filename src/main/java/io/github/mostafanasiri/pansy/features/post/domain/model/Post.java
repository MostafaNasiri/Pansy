package io.github.mostafanasiri.pansy.features.post.domain.model;

import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class Post {
    private Integer id;
    private User user;
    private String caption;
    private List<Image> images;
    private Integer likeCount;
    private Integer commentCount;
    private Date createdAt;
    private boolean isLikedByAuthenticatedUser;

    public Post(String caption, List<Image> images) {
        this.caption = caption;
        this.images = images;
    }

    public Post(Integer id, String caption, List<Image> images) {
        this.id = id;
        this.caption = caption;
        this.images = images;
    }

    public Post(
            Integer id,
            User user,
            String caption,
            List<Image> images,
            Integer likeCount,
            Integer commentCount,
            Date createdAt
    ) {
        this.id = id;
        this.user = user;
        this.caption = caption;
        this.images = images;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }

    public void setLikedByAuthenticatedUser(boolean likedByAuthenticatedUser) {
        isLikedByAuthenticatedUser = likedByAuthenticatedUser;
    }
}
