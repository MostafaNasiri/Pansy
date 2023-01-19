package io.github.mostafanasiri.pansy.features.notification.data.repository;

import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {
    @Query("SELECT n FROM CommentNotificationEntity n WHERE n.comment.id=?1")
    Optional<CommentNotificationEntity> getCommentNotification(int commentId);

    @Query("SELECT n FROM LikeNotificationEntity n WHERE n.notifierUser.id=?1 AND n.post.id=?2")
    Optional<LikeNotificationEntity> getLikeNotification(int notifierUserId, int postId);
}
