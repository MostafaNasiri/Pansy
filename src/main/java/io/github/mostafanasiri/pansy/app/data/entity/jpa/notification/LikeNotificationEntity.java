package io.github.mostafanasiri.pansy.app.data.entity.jpa.notification;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Getter
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
