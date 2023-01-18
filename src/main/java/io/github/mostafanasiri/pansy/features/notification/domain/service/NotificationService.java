package io.github.mostafanasiri.pansy.features.notification.domain.service;

import io.github.mostafanasiri.pansy.features.notification.data.repository.NotificationRepository;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository repository;

    public void addCommentNotification(CommentNotification notification) {

    }

    public void addLikeNotification(LikeNotification notification) {

    }

    public void addFollowNotification(FollowNotification notification) {

    }
}
