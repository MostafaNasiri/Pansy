package io.github.mostafanasiri.pansy.app.domain.service;

import io.github.mostafanasiri.pansy.app.common.BaseService;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.FeedEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.FileEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.FollowerEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.app.data.entity.redis.UserRedis;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FeedJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FileJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.app.domain.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.app.domain.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.app.domain.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.app.domain.mapper.UserDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.File;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.model.notification.FollowNotification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.search.mapper.orm.Search;
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

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDomainMapper userDomainMapper;

    public @NonNull List<User> searchInUsers(@NonNull String name, int page, int size) {
        var searchSession = Search.session(entityManager);
        var pageRequest = PageRequest.of(page, size);

        var searchResult = searchSession.search(UserEntity.class)
                .where(f -> f.bool()
                        .should(f.match()
                                .field("username")
                                .matching(name)
                                .fuzzy(1))
                        .should(f.match()
                                .field("fullName")
                                .matching(name)
                                .fuzzy(1)))
                .fetchHits((int) pageRequest.getOffset(), pageRequest.getPageSize());

        return userDomainMapper.userEntitiesToUsers(searchResult);
    }

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

    @Transactional
    public @NonNull User createUser(@NonNull User user) {
        if (userJpaRepository.findByUsername(user.username()).isPresent()) {
            throw new InvalidInputException("Username already exists");
        }

        var hashedPassword = passwordEncoder.encode(user.password());
        var userEntity = new UserEntity(user.fullName(), user.username(), hashedPassword);

        // Create a feed for user
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

        var sourceUserEntity = getUserEntity(getAuthenticatedUserId());
        var targetUserEntity = getUserEntity(targetUserId);

        var sourceUserHasNotFollowedTargetUser =
                followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId).isEmpty();

        if (sourceUserHasNotFollowedTargetUser) {
            var follower = new FollowerEntity(
                    sourceUserEntity,
                    targetUserEntity
            );
            followerJpaRepository.save(follower);

            updateFollowingCount(sourceUserEntity);
            updateFollowerCount(targetUserEntity);

            addFollowNotification(sourceUserId, targetUserId);
        }
    }

    private void addFollowNotification(int sourceUserId, int targetUserId) {
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

        var sourceUserEntity = getUserEntity(getAuthenticatedUserId());
        var targetUserEntity = getUserEntity(targetUserId);

        followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId)
                .ifPresent(followerEntity -> {
                    followerJpaRepository.delete(followerEntity);

                    updateFollowingCount(sourceUserEntity);
                    updateFollowerCount(targetUserEntity);

                    notificationService.deleteFollowNotification(sourceUserId, targetUserId);
                    feedService.removeAllPostsFromFeed(sourceUserId, targetUserId);
                });
    }

    private void updateFollowingCount(UserEntity userEntity) {
        var count = followerJpaRepository.getFollowingCount(userEntity.getId());

        userEntity.setFollowingCount(count);
        var updatedUser = userDomainMapper.userEntityToUser(userJpaRepository.save(userEntity));

        saveUserInRedis(updatedUser);
    }

    private void updateFollowerCount(UserEntity userEntity) {
        var count = followerJpaRepository.getFollowerCount(userEntity.getId());

        userEntity.setFollowerCount(count);
        var updatedUser = userDomainMapper.userEntityToUser(userJpaRepository.save(userEntity));

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

    private UserEntity getUserEntity(int userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }

    private FileEntity getFileEntity(int fileId) {
        return fileJpaRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException(File.class, fileId));
    }
}
