package io.github.mostafanasiri.pansy.app.domain.model;

import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class Post {
    private Integer id;
    private User user;
    private String caption;
    private List<Image> images;
    private int likeCount;
    private int commentCount;
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
            int likeCount,
            int commentCount,
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
