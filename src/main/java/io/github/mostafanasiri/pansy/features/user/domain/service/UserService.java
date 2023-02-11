package io.github.mostafanasiri.pansy.features.user.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.data.FileEntity;
import io.github.mostafanasiri.pansy.features.file.data.FileJpaRepository;
import io.github.mostafanasiri.pansy.features.file.domain.File;
import io.github.mostafanasiri.pansy.features.file.domain.FileService;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.FeedEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.FeedJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.redis.UserRedis;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.features.user.domain.UserDomainMapper;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private FeedJpaRepository feedJpaRepository;
    @Autowired
    private FileJpaRepository fileJpaRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDomainMapper userDomainMapper;

    public @NonNull List<User> getUsers(List<Integer> userIds) {
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

    public void updateUserFollowerCount(int userId, int count) {
        var userEntity = getUserEntity(userId);
        userEntity.setFollowerCount(count);

        var updatedUser = userDomainMapper.userEntityToUser(userJpaRepository.save(userEntity));
        saveUserInRedis(updatedUser);
    }

    public void updateUserFollowingCount(int userId, int count) {
        var userEntity = getUserEntity(userId);
        userEntity.setFollowingCount(count);

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
