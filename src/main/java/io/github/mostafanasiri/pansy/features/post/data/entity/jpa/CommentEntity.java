package io.github.mostafanasiri.pansy.features.post.data.entity.jpa;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@NoArgsConstructor
@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(columnList = "post_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class CommentEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @Column(name = "text", nullable = false, length = 500)
    private String text;

    // I added this field so that I can set CascadeType.REMOVE
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "comment")
    private List<CommentNotificationEntity> notifications;

    public CommentEntity(UserEntity user, PostEntity post, String text) {
        this.user = user;
        this.post = post;
        this.text = text;
    }

    public UserEntity getUser() {
        return user;
    }

    public PostEntity getPost() {
        return post;
    }

    public String getText() {
        return text;
    }
}
