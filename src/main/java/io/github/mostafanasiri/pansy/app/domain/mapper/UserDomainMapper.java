package io.github.mostafanasiri.pansy.app.domain.mapper;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.app.data.entity.redis.UserRedis;
import io.github.mostafanasiri.pansy.app.domain.model.Image;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDomainMapper {
    public List<User> userEntitiesToUsers(List<UserEntity> userEntities) {
        return userEntities.stream()
                .map(this::userEntityToUser)
                .toList();
    }

    public User userEntityToUser(@NonNull UserEntity userEntity) {
        Image avatarImage = null;

        var avatarEntity = userEntity.getAvatar();
        if (avatarEntity != null) {
            avatarImage = new Image(avatarEntity.getId(), avatarEntity.getName());
        }

        return new User(
                userEntity.getId(),
                userEntity.getFullName(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                avatarImage,
                userEntity.getBio(),
                userEntity.getPostCount(),
                userEntity.getFollowerCount(),
                userEntity.getFollowingCount()
        );
    }

    public List<User> usersRedisToUsers(List<UserRedis> usersRedis) {
        return usersRedis.stream()
                .map(this::userRedisToUser)
                .toList();
    }

    public User userRedisToUser(@NonNull UserRedis userRedis) {
        return new User(
                userRedis.getId(),
                userRedis.getUsername(),
                userRedis.getUsername(),
                null,
                new Image(null, userRedis.getAvatarName()),
                userRedis.getBio(),
                userRedis.getPostCount(),
                userRedis.getFollowerCount(),
                userRedis.getFollowingCount()
        );
    }

    public List<UserRedis> usersToUsersRedis(List<User> users) {
        return users.stream()
                .map(this::userToUserRedis)
                .toList();
    }

    public UserRedis userToUserRedis(@NonNull User user) {
        var avatarName = user.avatar() != null ? user.avatar().name() : null;

        return new UserRedis(
                user.id(),
                user.username(),
                user.fullName(),
                avatarName,
                user.bio(),
                user.postCount(),
                user.followerCount(),
                user.followingCount()
        );
    }
}
