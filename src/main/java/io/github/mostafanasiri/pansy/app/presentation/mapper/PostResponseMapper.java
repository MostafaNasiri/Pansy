package io.github.mostafanasiri.pansy.app.presentation.mapper;

import io.github.mostafanasiri.pansy.app.domain.model.Comment;
import io.github.mostafanasiri.pansy.app.domain.model.Post;
import io.github.mostafanasiri.pansy.app.presentation.FileUtils;
import io.github.mostafanasiri.pansy.app.presentation.response.CommentResponse;
import io.github.mostafanasiri.pansy.app.presentation.response.PostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PostResponseMapper {
    @Autowired
    private UserResponseMapper userResponseMapper;
    @Autowired
    private FileUtils fileUtils;

    public PostResponse mapFromPostModel(@NonNull Post post) {
        var user = userResponseMapper.userToMinimalUserResponse(post.getUser());

        var imageUrls = post.getImages()
                .stream()
                .map((i) -> fileUtils.createFileUrl(i.name()))
                .toList();

        return new PostResponse(
                post.getId(),
                user,
                post.getCaption(),
                imageUrls,
                0,
                0,
                post.isLikedByAuthenticatedUser(),
                post.getCreatedAt()
        );
    }

    public CommentResponse mapFromCommentModel(@NonNull Comment comment) {
        var user = userResponseMapper.userToMinimalUserResponse(comment.user());

        return new CommentResponse(
                comment.id(),
                user,
                comment.text(),
                comment.createdAt()
        );
    }
}
