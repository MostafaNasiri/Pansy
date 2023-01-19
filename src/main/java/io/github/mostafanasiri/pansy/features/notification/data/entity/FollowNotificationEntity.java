package io.github.mostafanasiri.pansy.features.notification.data.entity;

import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.springframework.lang.NonNull;

@Entity
@DiscriminatorValue("follow")
public class FollowNotificationEntity extends NotificationEntity {
    public FollowNotificationEntity(@NonNull UserEntity notifierUser, @NonNull UserEntity notifiedUser) {
        super(notifierUser, notifiedUser);
    }
}
