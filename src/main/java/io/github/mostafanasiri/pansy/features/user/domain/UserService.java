package io.github.mostafanasiri.pansy.features.user.domain;

import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.user.data.entity.FollowerEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.FollowerRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.UserRepository;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private FileService fileService;

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

    public User updateUser(int currentUserId, @NonNull User user) {
        var userEntity = getUserEntity(user.id());

        if (userEntity.getId() != currentUserId) {
            throw new AuthorizationException("Unauthorized user.");
        }

        if (user.avatar() != null) {
            var fileEntity = fileService.getFile(user.avatar().id());

            // Make sure that the avatar file does not belong to another user
            var userId = userRepository.getUserIdByAvatarFileId(fileEntity.getId());
            if (userId.isPresent()) {
                throw new InvalidInputException(
                        String.format(
                                "File with id %s is already set as an avatar.",
                                fileEntity.getId()
                        )
                );
            }

            userEntity.setAvatar(fileEntity);
        }

        userEntity.setFullName(user.fullName());
        userEntity.setBio(user.bio());

        return modelMapper.mapFromUserEntity(userRepository.save(userEntity));
    }

    public User getUser(int userId) {
        return modelMapper.mapFromUserEntity(getUserEntity(userId));
    }

    public List<User> getFollowers(int userId) {
        var userEntity = getUserEntity(userId);

        return followerRepository.findAllByTargetUser(userEntity)
                .stream()
                .map((f) -> modelMapper.mapFromUserEntity(f.getSourceUser()))
                .toList();
    }

    public List<User> getFollowing(int userId) {
        var userEntity = getUserEntity(userId);

        return followerRepository.findAllBySourceUser(userEntity)
                .stream()
                .map((f) -> modelMapper.mapFromUserEntity(f.getTargetUser()))
                .toList();
    }

    @Transactional
    public void followUser(int currentUserId, int sourceUserId, int targetUserId) {
        if (currentUserId != sourceUserId) {
            throw new AuthorizationException("Forbidden action.");
        }

        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't follow him/herself!");
        }

        var sourceUser = getUserEntity(sourceUserId);
        var targetUser = getUserEntity(targetUserId);

        var sourceUserHasNotFollowedTargetUser =
                followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser) == null;

        if (sourceUserHasNotFollowedTargetUser) {
            var follower = new FollowerEntity(sourceUser, targetUser);
            followerRepository.save(follower);

            sourceUser.incrementFollowingCount();
            userRepository.save(sourceUser);

            targetUser.incrementFollowerCount();
            userRepository.save(targetUser);
        }
    }

    @Transactional
    public void unfollowUser(int currentUserId, int sourceUserId, int targetUserId) {
        if (currentUserId != sourceUserId) {
            throw new AuthorizationException("Forbidden action.");
        }

        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't unfollow him/herself!");
        }

        var sourceUser = getUserEntity(sourceUserId);
        var targetUser = getUserEntity(targetUserId);

        var follower = followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser);

        if (follower != null) {
            followerRepository.delete(follower);

            sourceUser.decrementFollowingCount();
            userRepository.save(sourceUser);

            targetUser.decrementFollowerCount();
            userRepository.save(targetUser);
        }
    }

    private UserEntity getUserEntity(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }
}
