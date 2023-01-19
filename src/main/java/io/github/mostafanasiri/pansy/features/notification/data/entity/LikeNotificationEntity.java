package io.github.mostafanasiri.pansy.features.notification.data.entity;

import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import jakarta.persistence.*;
import org.springframework.lang.NonNull;

@Entity
@DiscriminatorValue("like")
public class LikeNotificationEntity extends NotificationEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    public LikeNotificationEntity(
            @NonNull UserEntity notifierUser,
            @NonNull UserEntity notifiedUser,
            @NonNull PostEntity post
    ) {
        super(notifierUser, notifiedUser);
        this.post = post;
    }
}
