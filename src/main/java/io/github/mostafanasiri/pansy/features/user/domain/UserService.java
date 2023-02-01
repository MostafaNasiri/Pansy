package io.github.mostafanasiri.pansy.features.user.domain;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.data.FileEntity;
import io.github.mostafanasiri.pansy.features.file.data.FileRepository;
import io.github.mostafanasiri.pansy.features.file.domain.File;
import io.github.mostafanasiri.pansy.features.file.domain.FileService;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.NotificationUser;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.FollowerEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.redis.RedisUserRepository;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String USERS_CACHE_NAME = "users";

    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private RedisUserRepository redisUserRepository;
    @Autowired
    private FollowerJpaRepository followerJpaRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DomainMapper domainMapper;

    public User getUser(int userId) {
        var redisUserOptional = redisUserRepository.findById(userId);
        if (redisUserOptional.isPresent()) {
            logger.debug(String.format("getUser - Fetching user %s from Redis", userId));
            return domainMapper.redisUserToUser(redisUserOptional.get());
        }

        logger.debug(String.format("getUser - Fetching user %s from database", userId));
        var user = domainMapper.userEntityToUser(getUserEntity(userId));

        addUserToRedis(user);

        return user;
    }

    private void addUserToRedis(User user) {
        var redisUser = domainMapper.userToRedisUser(user);
        redisUserRepository.save(redisUser);
    }

    public User createUser(@NonNull User user) {
        if (userJpaRepository.findByUsername(user.username()).isPresent()) {
            throw new InvalidInputException("Username already exists");
        }

        var hashedPassword = passwordEncoder.encode(user.password());
        var userEntity = new UserEntity(user.fullName(), user.username(), hashedPassword);

        return domainMapper.userEntityToUser(userJpaRepository.save(userEntity));
    }

    @CachePut(value = USERS_CACHE_NAME, key = "#user.id")
    public User updateUser(@NonNull User user) {
        if (getAuthenticatedUserId() != user.id()) {
            throw new AuthorizationException("Forbidden action");
        }

        var userEntity = getAuthenticatedUser();

        if (user.avatar() != null) {
            var fileEntity = getFileEntity(user.avatar().id());
            fileService.checkIfFilesAreAlreadyAttachedToAnEntity(List.of(fileEntity.getId()));
            userEntity.setAvatar(fileEntity);
        }

        userEntity.setFullName(user.fullName());
        userEntity.setBio(user.bio());

        return domainMapper.userEntityToUser(userJpaRepository.save(userEntity));
    }

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

            incrementFollowerCount(sourceUser);
            incrementFollowingCount(targetUser);

            createFollowNotification(sourceUserId, targetUserId);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    @CachePut(value = USERS_CACHE_NAME, key = "#user.getId")
    private User incrementFollowerCount(UserEntity user) {
        user.incrementFollowerCount();
        var result = userJpaRepository.save(user);

        // The returned value is only used for updating cache
        return domainMapper.userEntityToUser(result);
    }

    @SuppressWarnings("UnusedReturnValue")
    @CachePut(value = USERS_CACHE_NAME, key = "#user.getId")
    private User incrementFollowingCount(UserEntity user) {
        user.incrementFollowingCount();
        var result = userJpaRepository.save(user);

        // The returned value is only used for updating cache
        return domainMapper.userEntityToUser(result);
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

    @SuppressWarnings("UnusedReturnValue")
    @CachePut(value = USERS_CACHE_NAME, key = "#user.getId")
    private User decrementFollowerCount(UserEntity user) {
        user.decrementFollowerCount();
        var result = userJpaRepository.save(user);

        // The returned value is only used for updating cache
        return domainMapper.userEntityToUser(result);
    }

    @SuppressWarnings("UnusedReturnValue")
    @CachePut(value = USERS_CACHE_NAME, key = "#user.getId")
    private User decrementFollowingCount(UserEntity user) {
        user.decrementFollowingCount();
        var result = userJpaRepository.save(user);

        // The returned value is only used for updating cache
        return domainMapper.userEntityToUser(result);
    }

    @SuppressWarnings("UnusedReturnValue")
    @CachePut(value = USERS_CACHE_NAME, key = "#userId")
    public User updateUserPostCount(int userId, int count) {
        var user = getUserEntity(userId);
        user.setPostCount(count);

        var updatedUser = userJpaRepository.save(user);

        // The returned value is only used for updating cache
        return domainMapper.userEntityToUser(updatedUser);
    }

    private UserEntity getUserEntity(int userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }

    private FileEntity getFileEntity(int fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException(File.class, fileId));
    }
}
