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
    private ModelMapper modelMapper;

    public User getUser(int userId) {
        return modelMapper.mapFromUserEntity(getUserEntity(userId));
    }

    public User createUser(@NonNull User user) {
        if (userJpaRepository.findByUsername(user.username()).isPresent()) {
            throw new InvalidInputException("Username already exists");
        }

        var hashedPassword = passwordEncoder.encode(user.password());
        var userEntity = new UserEntity(user.fullName(), user.username(), hashedPassword);

        return modelMapper.mapFromUserEntity(userJpaRepository.save(userEntity));
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

        return modelMapper.mapFromUserEntity(userJpaRepository.save(userEntity));
    }

    public List<User> getFollowers(int userId, int page, int size) {
        var userEntity = getUserEntity(userId);
        var pageRequest = PageRequest.of(page, size);

        return followerJpaRepository.getFollowers(userEntity, pageRequest)
                .stream()
                .map((f) -> modelMapper.mapFromUserEntity(f.getSourceUser()))
                .toList();
    }

    public List<User> getFollowing(int userId, int page, int size) {
        var userEntity = getUserEntity(userId);
        var pageRequest = PageRequest.of(page, size);

        return followerJpaRepository.getFollowing(userEntity, pageRequest)
                .stream()
                .map((f) -> modelMapper.mapFromUserEntity(f.getTargetUser()))
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
        return modelMapper.mapFromUserEntity(result);
    }

    @SuppressWarnings("UnusedReturnValue")
    @CachePut(value = USERS_CACHE_NAME, key = "#user.getId")
    private User incrementFollowingCount(UserEntity user) {
        user.incrementFollowingCount();
        var result = userJpaRepository.save(user);

        // The returned value is only used for updating cache
        return modelMapper.mapFromUserEntity(result);
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
        return modelMapper.mapFromUserEntity(result);
    }

    @SuppressWarnings("UnusedReturnValue")
    @CachePut(value = USERS_CACHE_NAME, key = "#user.getId")
    private User decrementFollowingCount(UserEntity user) {
        user.decrementFollowingCount();
        var result = userJpaRepository.save(user);

        // The returned value is only used for updating cache
        return modelMapper.mapFromUserEntity(result);
    }

    @SuppressWarnings("UnusedReturnValue")
    @CachePut(value = USERS_CACHE_NAME, key = "#userId")
    public User updateUserPostCount(int userId, int count) {
        var user = getUserEntity(userId);
        user.setPostCount(count);

        var updatedUser = userJpaRepository.save(user);

        // The returned value is only used for updating cache
        return modelMapper.mapFromUserEntity(updatedUser);
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
