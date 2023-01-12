package io.github.mostafanasiri.pansy.features.user;

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
        if (userRepository.findByUsername(userEntity.getUsername()) != null) {
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

    public void followUser(int sourceUserId, int targetUserId) {
        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't follow him/herself!");
        }

        var sourceUser = getUser(sourceUserId);
        var targetUser = getUser(targetUserId);

        if (followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser) == null) {
            var follower = new Follower(sourceUser, targetUser);
            followerRepository.save(follower);
        }
    }

    public void unfollowUser(int sourceUserId, int targetUserId) {
        if (sourceUserId == targetUserId) {
            throw new InvalidInputException("A user can't unfollow him/herself!");
        }

        var sourceUser = getUser(sourceUserId);
        var targetUser = getUser(targetUserId);

        var follower = followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser);
        if (follower != null) {
            followerRepository.delete(follower);
        }
    }
}
