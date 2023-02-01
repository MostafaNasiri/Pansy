package io.github.mostafanasiri.pansy.features.user.domain;

import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.redis.RedisUser;
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

    public User redisUserToUser(@NonNull RedisUser redisUser) {
        return new User(
                redisUser.getId(),
                redisUser.getUsername(),
                redisUser.getUsername(),
                null,
                new Image(null, redisUser.getAvatarName()),
                redisUser.getBio(),
                redisUser.getPostCount(),
                redisUser.getFollowerCount(),
                redisUser.getFollowingCount()
        );
    }

    public RedisUser userToRedisUser(@NonNull User user) {
        return new RedisUser(
                user.id(),
                user.username(),
                user.fullName(),
                user.avatar().name(),
                user.bio(),
                user.postCount(),
                user.followerCount(),
                user.followingCount()
        );
    }
}
