package io.github.mostafanasiri.pansy.features.user.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.post.domain.service.FeedService;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.FollowerEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowService extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private UserService userService;
    @Autowired
    private FeedService feedService;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private FollowerJpaRepository followerJpaRepository;
    @Autowired
    private NotificationService notificationService;

    public List<User> getFollowers(int userId, int page, int size) {
        var user = userService.getUser(userId);

        var pageRequest = PageRequest.of(page, size);
        var followerIds = followerJpaRepository.getFollowerIds(user.id(), pageRequest);

        return userService.getUsers(followerIds);
    }

    public List<User> getFollowing(int userId, int page, int size) {
        var user = userService.getUser(userId);

        var pageRequest = PageRequest.of(page, size);
        var followingIds = followerJpaRepository.getFollowingIds(user.id(), pageRequest);

        return userService.getUsers(followingIds);
    }

    @Transactional
    public void followUser(int sourceUserId, int targetUserId) {
        if (getAuthenticatedUserId() != sourceUserId) {
            throw new AuthorizationException("Forbidden action");
        }

        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't follow him/herself!");
        }

        var sourceUser = userService.getUser(getAuthenticatedUserId());
        var targetUser = userService.getUser(targetUserId);

        var sourceUserHasNotFollowedTargetUser =
                followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId).isEmpty();

        if (sourceUserHasNotFollowedTargetUser) {
            var follower = new FollowerEntity(
                    userJpaRepository.getReferenceById(sourceUser.id()),
                    userJpaRepository.getReferenceById(targetUser.id())
            );
            followerJpaRepository.save(follower);

            updateFollowingCount(sourceUserId);
            updateFollowerCount(targetUserId);

            createFollowNotification(sourceUserId, targetUserId);
        }
    }

    private void createFollowNotification(int sourceUserId, int targetUserId) {
        var notification = new FollowNotification(new User(sourceUserId), new User(targetUserId));
        notificationService.addFollowNotification(notification);
    }

    @Transactional
    public void unfollowUser(int sourceUserId, int targetUserId) {
        if (getAuthenticatedUserId() != sourceUserId) {
            throw new AuthorizationException("Forbidden action");
        }

        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't unfollow him/herself!");
        }

        var sourceUser = userService.getUser(getAuthenticatedUserId());
        var targetUser = userService.getUser(targetUserId);

        followerJpaRepository.findBySourceUserAndTargetUser(sourceUser.id(), targetUser.id())
                .ifPresent(followerEntity -> {
                    followerJpaRepository.delete(followerEntity);

                    updateFollowingCount(sourceUserId);
                    updateFollowerCount(targetUserId);

                    notificationService.deleteFollowNotification(sourceUserId, targetUserId);
                    feedService.removeAllPostsFromFeed(sourceUserId, targetUserId);
                });
    }

    private void updateFollowingCount(int userId) {
        var count = followerJpaRepository.getFollowingCount(userId);
        userService.updateUserFollowingCount(userId, count);
    }

    private void updateFollowerCount(int userId) {
        var count = followerJpaRepository.getFollowerCount(userId);
        userService.updateUserFollowerCount(userId, count);
    }
}
