package io.github.mostafanasiri.pansy.features.user.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.NotificationUser;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.FollowerEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.features.user.domain.DomainMapper;
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
    private UserJpaRepository userJpaRepository;
    @Autowired
    private UserRedisRepository userRedisRepository;
    @Autowired
    private FollowerJpaRepository followerJpaRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private DomainMapper domainMapper;

    public List<User> getFollowers(int userId, int page, int size) {
        var userEntity = getUserEntity(userId);
        var pageRequest = PageRequest.of(page, size);

        return followerJpaRepository.getFollowers(userEntity, pageRequest)
                .stream()
                .map((f) -> domainMapper.userEntityToUser(f.getSourceUser()))
                .toList();
    }

    public List<User> getFollowing(int userId, int page, int size) {
        var userEntity = getUserEntity(userId);
        var pageRequest = PageRequest.of(page, size);

        return followerJpaRepository.getFollowing(userEntity, pageRequest)
                .stream()
                .map((f) -> domainMapper.userEntityToUser(f.getTargetUser()))
                .toList();
    }

    @Transactional
    public void followUser(int sourceUserId, int targetUserId) {
        if (getAuthenticatedUserId() != sourceUserId) {
            throw new AuthorizationException("Forbidden action");
        }

        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't follow him/herself!");
        }

        var sourceUser = getUserEntity(getAuthenticatedUserId());
        var targetUser = getUserEntity(targetUserId);

        var sourceUserHasNotFollowedTargetUser =
                followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser) == null;

        if (sourceUserHasNotFollowedTargetUser) {
            var follower = new FollowerEntity(sourceUser, targetUser);
            followerJpaRepository.save(follower);

            incrementFollowingCount(sourceUser);
            incrementFollowerCount(targetUser);

            createFollowNotification(sourceUserId, targetUserId);
        }
    }

    private void incrementFollowingCount(UserEntity userEntity) {
        userEntity.incrementFollowingCount();

        var user = domainMapper.userEntityToUser(userJpaRepository.save(userEntity));
        saveUserInRedis(user);
    }

    private void incrementFollowerCount(UserEntity userEntity) {
        userEntity.incrementFollowerCount();

        var user = domainMapper.userEntityToUser(userJpaRepository.save(userEntity));
        saveUserInRedis(user);
    }

    private void createFollowNotification(int sourceUserId, int targetUserId) {
        var notification = new FollowNotification(
                new NotificationUser(sourceUserId),
                new NotificationUser(targetUserId)
        );
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

        var sourceUser = getUserEntity(getAuthenticatedUserId());
        var targetUser = getUserEntity(targetUserId);

        var follower = followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser);

        if (follower != null) {
            followerJpaRepository.delete(follower);

            decrementFollowingCount(sourceUser);
            decrementFollowerCount(targetUser);

            notificationService.deleteFollowNotification(sourceUserId, targetUserId);
        }
    }

    private void decrementFollowingCount(UserEntity userEntity) {
        userEntity.decrementFollowingCount();

        var user = domainMapper.userEntityToUser(userJpaRepository.save(userEntity));
        saveUserInRedis(user);
    }

    private void decrementFollowerCount(UserEntity userEntity) {
        userEntity.decrementFollowerCount();

        var user = domainMapper.userEntityToUser(userJpaRepository.save(userEntity));
        saveUserInRedis(user);
    }

    private void saveUserInRedis(User user) {
        logger.info(String.format("Saving user %s in Redis", user.id()));

        var userRedis = domainMapper.userToUserRedis(user);
        userRedisRepository.save(userRedis);
    }

    private UserEntity getUserEntity(int userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }
}
