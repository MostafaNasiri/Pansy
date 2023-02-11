package io.github.mostafanasiri.pansy.features.post.presentation;

import io.github.mostafanasiri.pansy.features.file.presentation.FileUtils;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.presentation.response.CommentResponse;
import io.github.mostafanasiri.pansy.features.post.presentation.response.PostResponse;
import io.github.mostafanasiri.pansy.features.post.presentation.response.UserResponse;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {
    @Autowired
    private FileUtils fileUtils;

    public PostResponse mapFromPostModel(@NonNull Post post) {
        var userResponse = mapFromUserModel(post.getUser());

        var imageUrls = post.getImages()
                .stream()
                .map((i) -> fileUtils.createFileUrl(i.name()))
                .toList();

        return new PostResponse(
                post.getId(),
                userResponse,
                post.getCaption(),
                imageUrls,
                post.getLikeCount() != null ? post.getLikeCount() : 0,
                post.getCommentCount() != null ? post.getCommentCount() : 0,
                post.getIsLikedByAuthenticatedUser() != null ? post.getIsLikedByAuthenticatedUser() : false,
                post.getCreatedAt()
        );
    }

    public UserResponse mapFromUserModel(@NonNull User user) {
        var avatarUrl = user.avatar() != null ? fileUtils.createFileUrl(user.avatar().name()) : null;

        return new UserResponse(
                user.id(),
                user.username(),
                avatarUrl
        );
    }

    public CommentResponse mapFromCommentModel(@NonNull Comment comment) {
        return new CommentResponse(
                comment.id(),
                mapFromUserModel(comment.user()),
                comment.text(),
                comment.createdAt()
        );
    }
}
