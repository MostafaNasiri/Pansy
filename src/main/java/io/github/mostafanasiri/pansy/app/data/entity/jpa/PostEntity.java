package io.github.mostafanasiri.pansy.app.data.entity.jpa;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.features.file.data.FileEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.NonNull;

import java.util.List;

@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "posts",
        indexes = {
                @Index(columnList = "user_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class PostEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "caption", nullable = false, length = 1000)
    private String caption;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_images",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private List<FileEntity> images;

    // I added this field so that I can set CascadeType.REMOVE
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "post")
    private List<LikeEntity> likes;

    // I added this field so that I can set CascadeType.REMOVE
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "post")
    private List<CommentEntity> comments;

    // I added this field so that I can set CascadeType.REMOVE
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "post")
    private List<LikeNotificationEntity> likeNotifications;

    // I added this field so that I can set CascadeType.REMOVE
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "post")
    private List<CommentNotificationEntity> commentNotifications;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    public PostEntity(@NonNull UserEntity user, @NonNull String caption, @NonNull List<FileEntity> images) {
        this.user = user;
        this.caption = caption;
        this.images = images;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getCaption() {
        return caption;
    }

    public List<FileEntity> getImages() {
        return images;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setImages(List<FileEntity> images) {
        this.images = images;
    }

    public boolean hasImage(int imageId) {
        return images.stream().anyMatch(i -> i.getId() == imageId);
    }

    public void setLikeCount(int likeCount) {
        if (likeCount >= 0) {
            this.likeCount = likeCount;
        }
    }

    public void setCommentCount(int commentCount) {
        if (commentCount >= 0) {
            this.commentCount = commentCount;
        }
    }
}
