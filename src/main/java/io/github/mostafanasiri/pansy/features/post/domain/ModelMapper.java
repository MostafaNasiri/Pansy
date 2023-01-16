package io.github.mostafanasiri.pansy.features.post.domain;

import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component("postFeatureModelMapper")
public class ModelMapper {
    public Post mapFromPostEntity(PostEntity entity, boolean isLikedByCurrentUser) {
        var user = mapFromUserEntity(entity.getUser());

        var images = entity.getImages()
                .stream()
                .map((i) -> new Image(i.getId(), i.getName()))
                .toList();

        return new Post(
                entity.getId(),
                user,
                entity.getCaption(),
                images,
                entity.getLikeCount(),
                isLikedByCurrentUser
        );
    }

    public Comment mapFromCommentEntity(CommentEntity entity) {
        var user = mapFromUserEntity(entity.getUser());

        return new Comment(entity.getId(), user, entity.getText(), entity.getCreatedAt());
    }

    public User mapFromUserEntity(UserEntity entity) {
        var avatarName = entity.getAvatar() != null ? entity.getAvatar().getName() : null;

        return new User(
                entity.getId(),
                entity.getFullName(),
                avatarName
        );
    }
}
