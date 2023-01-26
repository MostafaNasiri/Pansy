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
import io.github.mostafanasiri.pansy.features.user.data.entity.FollowerEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.FollowerRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.UserRepository;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService extends BaseService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowerRepository followerRepository;

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

    public User createUser(@NonNull User user) {
        if (userRepository.findByUsername(user.username()).isPresent()) {
            throw new InvalidInputException("Username already exists");
        }

        var hashedPassword = passwordEncoder.encode(user.password());
        var userEntity = new UserEntity(user.fullName(), user.username(), hashedPassword);

        return modelMapper.mapFromUserEntity(userRepository.save(userEntity));
    }

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

        return modelMapper.mapFromUserEntity(userRepository.save(userEntity));
    }

    public User getPublicUserData(int userId) {
        return modelMapper.mapFromUserEntity(getUserEntity(userId));
    }

    public List<User> getFollowers(int userId, int page, int size) {
        var userEntity = getUserEntity(userId);
        var pageRequest = PageRequest.of(page, size);

        return followerRepository.getFollowers(userEntity, pageRequest)
                .stream()
                .map((f) -> modelMapper.mapFromUserEntity(f.getSourceUser()))
                .toList();
    }

    public List<User> getFollowing(int userId, int page, int size) {
        var userEntity = getUserEntity(userId);
        var pageRequest = PageRequest.of(page, size);

        return followerRepository.getFollowing(userEntity, pageRequest)
                .stream()
                .map((f) -> modelMapper.mapFromUserEntity(f.getTargetUser()))
                .toList();
    }

    @Transactional
    public void followUser(int sourceUserId, int targetUserId) { // TODO: Remove sourceUserId
        if (getAuthenticatedUserId() != sourceUserId) {
            throw new AuthorizationException("Forbidden action");
        }

        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't follow him/herself!");
        }

        var sourceUser = getAuthenticatedUser();
        var targetUser = getUserEntity(targetUserId);

        var sourceUserHasNotFollowedTargetUser =
                followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser) == null;

        if (sourceUserHasNotFollowedTargetUser) {
            var follower = new FollowerEntity(sourceUser, targetUser);
            followerRepository.save(follower);

            incrementFollowingFollowerCount(sourceUser, targetUser);

            // Add a new notification for the followed user
            var notification = new FollowNotification(
                    new NotificationUser(sourceUserId),
                    new NotificationUser(targetUserId)
            );
            notificationService.addFollowNotification(notification);
        }
    }

    private void incrementFollowingFollowerCount(UserEntity sourceUser, UserEntity targetUser) {
        sourceUser.incrementFollowingCount();
        userRepository.save(sourceUser);

        targetUser.incrementFollowerCount();
        userRepository.save(targetUser);
    }

    @Transactional
    public void unfollowUser(int sourceUserId, int targetUserId) { // TODO: Remove sourceUserId
        if (getAuthenticatedUserId() != sourceUserId) {
            throw new AuthorizationException("Forbidden action");
        }

        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't unfollow him/herself!");
        }

        var sourceUser = getAuthenticatedUser();
        var targetUser = getUserEntity(targetUserId);

        var follower = followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser);

        if (follower != null) {
            followerRepository.delete(follower);
            decrementFollowingFollowerCount(sourceUser, targetUser);
            notificationService.deleteFollowNotification(sourceUserId, targetUserId);
        }
    }

    private void decrementFollowingFollowerCount(UserEntity sourceUser, UserEntity targetUser) {
        sourceUser.decrementFollowingCount();
        userRepository.save(sourceUser);

        targetUser.decrementFollowerCount();
        userRepository.save(targetUser);
    }

    private UserEntity getUserEntity(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }

    private FileEntity getFileEntity(int fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException(File.class, fileId));
    }
}