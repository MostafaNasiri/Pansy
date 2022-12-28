package io.github.mostafanasiri.pansy.features.user;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.user.entity.Follower;
import io.github.mostafanasiri.pansy.features.user.entity.User;
import io.github.mostafanasiri.pansy.features.user.repo.FollowerRepository;
import io.github.mostafanasiri.pansy.features.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowerRepository followerRepository;

    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new InvalidInputException("Username already exists");
        }

        // TODO hash user's password

        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User getUser(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException(User.class, userId)
                );
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
}
