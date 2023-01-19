package io.github.mostafanasiri.pansy.features.notification.data.repository;

import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {
    Optional<CommentNotificationEntity> getCommentNotificationByCommentId(int commentId);
}
