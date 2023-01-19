package io.github.mostafanasiri.pansy.features.notification.data.entity;

import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import jakarta.persistence.*;
import org.springframework.lang.NonNull;

@Entity
@DiscriminatorValue("comment")
public class CommentNotificationEntity extends NotificationEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentEntity comment;

    public CommentNotificationEntity(
            @NonNull UserEntity notifierUser,
            @NonNull UserEntity notifiedUser,
            @NonNull PostEntity post,
            @NonNull CommentEntity comment
    ) {
        super(notifierUser, notifiedUser);
        this.post = post;
        this.comment = comment;
    }
}
