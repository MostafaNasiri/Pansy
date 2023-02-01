package io.github.mostafanasiri.pansy.features.user.domain;

import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.redis.UserRedis;
import io.github.mostafanasiri.pansy.features.user.domain.model.Image;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component("userFeatureModelMapper")
public class DomainMapper {
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
