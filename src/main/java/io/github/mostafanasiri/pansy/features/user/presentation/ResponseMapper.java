package io.github.mostafanasiri.pansy.features.user.presentation;

import io.github.mostafanasiri.pansy.features.file.FileUtils;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.presentation.response.GetFollowersFollowingResponse;
import io.github.mostafanasiri.pansy.features.user.presentation.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("userFeatureResponseMapper")
public class ResponseMapper {
    @Autowired
    private FileUtils fileUtils;

    public UserResponse fromUserModel(User user) {
        var avatarUrl = user.avatar() != null ? fileUtils.createFileUrl(user.avatar().name()) : null;

        return new UserResponse(
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

    public GetFollowersFollowingResponse fromUserModels(List<User> users) {
        return new GetFollowersFollowingResponse(
                users.stream()
                        .map(u -> {
                            var avatarUrl = u.avatar() != null ? fileUtils.createFileUrl(u.avatar().name()) : null;

                            return new GetFollowersFollowingResponse.Item(
                                    u.id(),
                                    u.fullName(),
                                    u.username(),
                                    avatarUrl
                            );
                        })
                        .toList()
        );
    }
}
