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
import io.github.mostafanasiri.pansy.features.user.data.repo.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final static String USERS_CACHE_NAME = "users";

    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private UserRedisRepository userRedisRepository;
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
        var userRedis = userRedisRepository.findById(userId);
        if (userRedis.isPresent()) {
            logger.info(String.format("getUser - Fetching user %s from Redis", userId));
            return domainMapper.userRedisToUser(userRedis.get());
        }

        logger.info(String.format("getUser - Fetching user %s from database", userId));
        var user = domainMapper.userEntityToUser(getUserEntity(userId));

        saveUserInRedis(user);

        return user;
    }

    public User createUser(@NonNull User user) {
        if (userJpaRepository.findByUsername(user.username()).isPresent()) {
            throw new InvalidInputException("Username already exists");
        }

        var hashedPassword = passwordEncoder.encode(user.password());
        var userEntity = new UserEntity(user.fullName(), user.username(), hashedPassword);

        var createdUser = domainMapper.userEntityToUser(userJpaRepository.save(userEntity));
        saveUserInRedis(createdUser);

        return createdUser;
    }

    public User updateUser(@NonNull User user) {
        if (getAuthenticatedUserId() != user.id()) {
            throw new AuthorizationException("Forbidden action");
        }

        var authenticatedUserEntity = getUserEntity(getAuthenticatedUserId());

        if (user.avatar() != null) {
            var fileEntity = getFileEntity(user.avatar().id());
            fileService.checkIfFilesAreAlreadyAttachedToAnEntity(List.of(fileEntity.getId()));
            authenticatedUserEntity.setAvatar(fileEntity);
        }

        authenticatedUserEntity.setFullName(user.fullName());
        authenticatedUserEntity.setBio(user.bio());

        var updatedUser = domainMapper.userEntityToUser(userJpaRepository.save(authenticatedUserEntity));
        saveUserInRedis(updatedUser);

        return updatedUser;
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

    public void updateUserPostCount(int userId, int count) {
        var user = getUserEntity(userId);
        user.setPostCount(count);

        var updatedUser = domainMapper.userEntityToUser(userJpaRepository.save(user));
        saveUserInRedis(updatedUser);
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

    private FileEntity getFileEntity(int fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException(File.class, fileId));
    }
}
