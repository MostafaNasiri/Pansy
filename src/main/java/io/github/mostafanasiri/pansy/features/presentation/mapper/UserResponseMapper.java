package io.github.mostafanasiri.pansy.features.presentation.mapper;

import io.github.mostafanasiri.pansy.features.file.presentation.FileUtils;
import io.github.mostafanasiri.pansy.features.presentation.response.user.FullUserResponse;
import io.github.mostafanasiri.pansy.features.presentation.response.user.MinimalUserResponse;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("userFeatureResponseMapper")
public class UserResponseMapper {
    @Autowired
    private FileUtils fileUtils;

    public FullUserResponse userToFullUserResponse(User user) {
        var avatarUrl = user.avatar() != null ? fileUtils.createFileUrl(user.avatar().name()) : null;

        return new FullUserResponse(
                user.id(),
                user.fullName(),
                user.username(),
                user.bio(),
                avatarUrl,
                user.postCount(),
                user.followerCount(),
                user.followingCount()
        );
    }

    public List<MinimalUserResponse> usersToMinimalUserResponses(List<User> users) {
        return users.stream()
                .map(this::userToMinimalUserResponse)
                .toList();
    }

    public MinimalUserResponse userToMinimalUserResponse(User user) {
        var avatarUrl = user.avatar() != null ? fileUtils.createFileUrl(user.avatar().name()) : null;

        return new MinimalUserResponse(
                user.id(),
                user.fullName(),
                user.username(),
                avatarUrl
        );
    }
}
