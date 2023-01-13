package io.github.mostafanasiri.pansy.features.post.presentation;

import io.github.mostafanasiri.pansy.features.file.FileUtils;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.post.presentation.response.CommentResponse;
import io.github.mostafanasiri.pansy.features.post.presentation.response.PostResponse;
import io.github.mostafanasiri.pansy.features.post.presentation.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {
    @Autowired
    private FileUtils fileUtils;

    public PostResponse mapFromPostModel(@NonNull Post post) {
        var userResponse = mapFromUserModel(post.user());

        var imageUrls = post.images()
                .stream()
                .map((i) -> fileUtils.createFileUrl(i.name()))
                .toList();

        var likesCount = post.likeData() != null ? post.likeData().likesCount() : 0;
        var isLikedByCurrentUser = post.likeData() != null ? post.likeData().isLikedByCurrentUser() : false;

        return new PostResponse(
                post.id(),
                userResponse,
                post.caption(),
                imageUrls,
                likesCount,
                isLikedByCurrentUser
        );
    }

    public UserResponse mapFromUserModel(@NonNull User user) {
        var avatarUrl = user.avatar() != null ? fileUtils.createFileUrl(user.avatar()) : null;

        return new UserResponse(
                user.id(),
                user.name(),
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
