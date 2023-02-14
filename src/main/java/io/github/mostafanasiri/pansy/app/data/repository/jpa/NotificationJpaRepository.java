package io.github.mostafanasiri.pansy.app.data.repository.jpa;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.FollowNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.NotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Integer> {
    @Query("""
            SELECT n, nu, p, c
            FROM NotificationEntity n
            INNER JOIN n.notifierUser nu
            LEFT JOIN nu.avatar
            LEFT JOIN n.post p
            LEFT JOIN n.comment c
            WHERE n.notifiedUser.id=?1
            ORDER BY n.createdAt DESC
            """)
    List<NotificationEntity> getNotifications(int notifiedUserId, Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead=true WHERE n.id IN(?1) AND n.isRead=false")
    void markNotificationsAsRead(List<Integer> ids);

    int countByNotifiedUserIdAndIsReadIsFalse(int notifiedUserID);

    @Query("SELECT n FROM CommentNotificationEntity n WHERE n.comment.id=?1")
    Optional<CommentNotificationEntity> getCommentNotification(int commentId);

    @Query("SELECT n FROM LikeNotificationEntity n WHERE n.notifierUser.id=?1 AND n.post.id=?2")
    Optional<LikeNotificationEntity> getLikeNotification(int notifierUserId, int postId);

    @Query("SELECT n FROM FollowNotificationEntity n WHERE n.notifierUser.id=?1 AND n.notifiedUser.id=?2")
    Optional<FollowNotificationEntity> getFollowNotification(int notifierUserId, int notifiedUserId);
}
