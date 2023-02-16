package io.github.mostafanasiri.pansy.app.data.entity.jpa.notification;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("follow")
public class FollowNotificationEntity extends NotificationEntity {
    public FollowNotificationEntity(@NonNull UserEntity notifierUser, @NonNull UserEntity notifiedUser) {
        super(notifierUser, notifiedUser);
    }
}
