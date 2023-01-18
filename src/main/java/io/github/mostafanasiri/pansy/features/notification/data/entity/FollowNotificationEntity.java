package io.github.mostafanasiri.pansy.features.notification.data.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("follow")
public class FollowNotificationEntity extends NotificationEntity {
}
