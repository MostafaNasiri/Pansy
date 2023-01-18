package io.github.mostafanasiri.pansy.features.notification.data.entity;

import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("like")
public class LikeNotificationEntity extends NotificationEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;
}
