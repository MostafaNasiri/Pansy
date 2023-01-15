package io.github.mostafanasiri.pansy.features.post.data.entity;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.features.file.File;
import io.github.mostafanasiri.pansy.features.user.entity.UserEntity;
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

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "post_images",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private List<File> images;

    // I added this field so that I can set CascadeType.REMOVE
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "post")
    private List<LikeEntity> likes;

    // I added this field so that I can set CascadeType.REMOVE
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "post")
    private List<CommentEntity> comments;

    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;

    public PostEntity(@NonNull UserEntity user, @NonNull String caption, @NonNull List<File> images) {
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

    public List<File> getImages() {
        return images;
    }

    public void incrementLikesCount() {
        likesCount++;
    }

    public void decrementLikesCount() {
        if (likesCount > 0) {
            likesCount--;
        }
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setImages(List<File> images) {
        this.images = images;
    }

    public boolean hasImage(int imageId) {
        return images.stream().anyMatch(i -> i.getId() == imageId);
    }
}
