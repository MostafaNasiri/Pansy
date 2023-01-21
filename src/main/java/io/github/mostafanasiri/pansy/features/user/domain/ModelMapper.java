package io.github.mostafanasiri.pansy.features.user.domain;

import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import io.github.mostafanasiri.pansy.features.user.domain.model.Image;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component("userFeatureModelMapper")
public class ModelMapper {
    public User mapFromUserEntity(UserEntity userEntity) {
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
}
