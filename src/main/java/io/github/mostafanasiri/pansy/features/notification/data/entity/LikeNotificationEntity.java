package io.github.mostafanasiri.pansy.features.notification.data.entity;

import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@NoArgsConstructor
@Entity
@DiscriminatorValue("like")
public class LikeNotificationEntity extends NotificationEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
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
