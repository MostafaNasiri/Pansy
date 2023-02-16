package io.github.mostafanasiri.pansy.app.data.entity.jpa;

import io.github.mostafanasiri.pansy.app.common.BaseEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.CommentNotificationEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
