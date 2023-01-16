package io.github.mostafanasiri.pansy.features.user;

import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.user.entity.Follower;
import io.github.mostafanasiri.pansy.features.user.entity.UserEntity;
import io.github.mostafanasiri.pansy.features.user.repo.FollowerRepository;
import io.github.mostafanasiri.pansy.features.user.repo.UserRepository;
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
    private PasswordEncoder passwordEncoder;

    public UserEntity createUser(@NonNull UserEntity userEntity) {
        if (userRepository.findByUsername(userEntity.getUsername()).isPresent()) {
            throw new InvalidInputException("Username already exists");
        }

        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));

        return userRepository.save(userEntity);
    }

    public UserEntity updateUser(@NonNull UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    public UserEntity getUser(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException(UserEntity.class, userId)
                );
    }

    public List<UserEntity> getFollowers(int userId) {
        var user = getUser(userId);

        return followerRepository.findAllByTargetUser(user)
                .stream()
                .map((f) -> f.getSourceUser())
                .toList();
    }

    public List<UserEntity> getFollowing(int userId) {
        var user = getUser(userId);

        return followerRepository.findAllBySourceUser(user)
                .stream()
                .map((f) -> f.getTargetUser())
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

        var sourceUser = getUser(sourceUserId);
        var targetUser = getUser(targetUserId);

        var sourceUserHasNotFollowedTargetUser =
                followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser) == null;

        if (sourceUserHasNotFollowedTargetUser) {
            var follower = new Follower(sourceUser, targetUser);
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

        var sourceUser = getUser(sourceUserId);
        var targetUser = getUser(targetUserId);

        var follower = followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser);

        if (follower != null) {
            followerRepository.delete(follower);

            sourceUser.decrementFollowingCount();
            userRepository.save(sourceUser);

            targetUser.decrementFollowerCount();
            userRepository.save(targetUser);
        }
    }
}
