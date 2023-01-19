package io.github.mostafanasiri.pansy.features.notification.data.repository;

import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.FollowNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.NotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {
    @Query("""
            SELECT n, nu, p, pi, c
            FROM NotificationEntity n
            INNER JOIN n.notifierUser nu
            LEFT JOIN nu.avatar
            LEFT JOIN n.post p
            LEFT JOIN p.images pi
            LEFT JOIN n.comment c
            """)
    List<NotificationEntity> getNotifications(int notifiedUserId, Pageable pageable);

    int countByNotifiedUserIdAndIsReadIsFalse(int notifiedUserID);

    @Query("SELECT n FROM CommentNotificationEntity n WHERE n.comment.id=?1")
    Optional<CommentNotificationEntity> getCommentNotification(int commentId);

    @Query("SELECT n FROM LikeNotificationEntity n WHERE n.notifierUser.id=?1 AND n.post.id=?2")
    Optional<LikeNotificationEntity> getLikeNotification(int notifierUserId, int postId);

    @Query("SELECT n FROM FollowNotificationEntity n WHERE n.notifierUser.id=?1 AND n.notifiedUser.id=?2")
    Optional<FollowNotificationEntity> getFollowNotification(int notifierUserId, int notifiedUserId);
}
