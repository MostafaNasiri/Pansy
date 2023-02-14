package io.github.mostafanasiri.pansy.app.domain.service;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.FeedEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.FollowerEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.app.data.entity.redis.UserRedis;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FeedJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.app.domain.mapper.UserDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.model.notification.FollowNotification;
import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.data.FileEntity;
import io.github.mostafanasiri.pansy.features.file.data.FileJpaRepository;
import io.github.mostafanasiri.pansy.features.file.domain.File;
import io.github.mostafanasiri.pansy.features.file.domain.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private UserRedisRepository userRedisRepository;
    @Autowired
    private FollowerJpaRepository followerJpaRepository;
    @Autowired
    private FeedJpaRepository feedJpaRepository;
    @Autowired
    private FileJpaRepository fileJpaRepository;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private FeedService feedService;
    @Autowired
    private FileService fileService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDomainMapper userDomainMapper;

    public @NonNull List<User> getUsers(@NonNull List<Integer> userIds) {
        var cachedUsers = getCachedUsers(userIds);
        var cachedUserIds = cachedUsers.stream()
                .map(User::id)
                .toList();

        var unCachedUsers = getUnCachedUsers(userIds, cachedUserIds);
        if (!unCachedUsers.isEmpty()) {
            saveUsersInRedis(unCachedUsers);
        }

        var result = new ArrayList<User>();
        result.addAll(unCachedUsers);
        result.addAll(cachedUsers);

        return result;
    }

    private List<User> getCachedUsers(List<Integer> userIds) {
        var cachedUsers = new ArrayList<UserRedis>();

        userRedisRepository.findAllById(userIds)
                .forEach(userRedis -> {
                    logger.info(String.format("getCachedUsers - Fetched user %s from Redis", userRedis.getId()));
                    cachedUsers.add(userRedis);
                });

        return userDomainMapper.usersRedisToUsers(cachedUsers);
    }

    private List<User> getUnCachedUsers(List<Integer> userIds, List<Integer> cachedUserIds) {
        var unCachedUserIds = new ArrayList<>(userIds);
        unCachedUserIds.removeAll(cachedUserIds);

        List<User> unCachedUsers = new ArrayList<>();

        if (!unCachedUserIds.isEmpty()) {
            logger.info(String.format("getUnCachedUsers - Fetching users %s from database", unCachedUserIds));

            var unCachedUserEntities = userJpaRepository.findAllById(unCachedUserIds);

            // Map uncached users to User models
            unCachedUsers = userDomainMapper.userEntitiesToUsers(unCachedUserEntities);
        }

        return unCachedUsers;
    }

    public @NonNull User getUser(int userId) {
        var userRedis = userRedisRepository.findById(userId);
        if (userRedis.isPresent()) {
            logger.info(String.format("getUser - Fetching user %s from Redis", userId));
            return userDomainMapper.userRedisToUser(userRedis.get());
        }

        logger.info(String.format("getUser - Fetching user %s from database", userId));
        var userEntity = getUserEntity(userId);
        var user = userDomainMapper.userEntityToUser(userEntity);

        saveUserInRedis(user);

        return user;
    }

    public @NonNull User createUser(@NonNull User user) {
        if (userJpaRepository.findByUsername(user.username()).isPresent()) {
            throw new InvalidInputException("Username already exists");
        }

        var hashedPassword = passwordEncoder.encode(user.password());
        var userEntity = new UserEntity(user.fullName(), user.username(), hashedPassword);

        var feedEntity = new FeedEntity(userEntity);
        feedJpaRepository.save(feedEntity);

        var createdUser = userDomainMapper.userEntityToUser(userJpaRepository.save(userEntity));
        saveUserInRedis(createdUser);

        return createdUser;
    }

    public @NonNull User updateUser(@NonNull User user) {
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

        var updatedUser = userDomainMapper.userEntityToUser(userJpaRepository.save(authenticatedUserEntity));
        saveUserInRedis(updatedUser);

        return updatedUser;
    }

    public void updateUserPostCount(int userId, int count) {
        var user = getUserEntity(userId);
        user.setPostCount(count);

        var updatedUser = userDomainMapper.userEntityToUser(userJpaRepository.save(user));
        saveUserInRedis(updatedUser);
    }

    private void saveUsersInRedis(List<User> users) {
        logger.info(String.format("Saving users %s in Redis", users.stream().map(User::id).toList()));

        var usersRedis = userDomainMapper.usersToUsersRedis(users);
        userRedisRepository.saveAll(usersRedis);
    }

    private void saveUserInRedis(User user) {
        logger.info(String.format("Saving user %s in Redis", user.id()));

        var userRedis = userDomainMapper.userToUserRedis(user);
        userRedisRepository.save(userRedis);
    }

    public @NonNull List<User> getFollowers(int userId, int page, int size) {
        var user = getUser(userId);

        var pageRequest = PageRequest.of(page, size);
        var followerIds = followerJpaRepository.getFollowerIds(user.id(), pageRequest);

        return getUsers(followerIds);
    }

    public @NonNull List<User> getFollowing(int userId, int page, int size) {
        var user = getUser(userId);

        var pageRequest = PageRequest.of(page, size);
        var followingIds = followerJpaRepository.getFollowingIds(user.id(), pageRequest);

        return getUsers(followingIds);
    }

    @Transactional
    public void followUser(int sourceUserId, int targetUserId) {
        if (getAuthenticatedUserId() != sourceUserId) {
            throw new AuthorizationException("Forbidden action");
        }

        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't follow him/herself!");
        }

        var sourceUser = getUser(getAuthenticatedUserId());
        var targetUser = getUser(targetUserId);

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

        var sourceUser = getUser(getAuthenticatedUserId());
        var targetUser = getUser(targetUserId);

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

        var userEntity = getUserEntity(userId);
        userEntity.setFollowingCount(count);

        var updatedUser = userDomainMapper.userEntityToUser(userJpaRepository.save(userEntity));
        saveUserInRedis(updatedUser);
    }

    private void updateFollowerCount(int userId) {
        var count = followerJpaRepository.getFollowerCount(userId);

        var userEntity = getUserEntity(userId);
        userEntity.setFollowerCount(count);

        var updatedUser = userDomainMapper.userEntityToUser(userJpaRepository.save(userEntity));
        saveUserInRedis(updatedUser);
    }

    private UserEntity getUserEntity(int userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }

    private FileEntity getFileEntity(int fileId) {
        return fileJpaRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException(File.class, fileId));
    }
}
