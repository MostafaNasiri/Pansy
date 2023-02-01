package io.github.mostafanasiri.pansy.features.post.domain;

import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("postFeatureModelMapper")
public class ModelMapper {
    public List<Post> mapUserPosts(
            UserEntity userEntity,
            List<PostEntity> postEntities,
            List<Integer> likedPostIds
    ) {
        var user = mapFromUserEntity(userEntity);

        return postEntities.stream()
                .map(pe -> {
                    var isLiked = likedPostIds.contains(pe.getId());
                    return mapFromPostEntity(user, pe, isLiked);
                })
                .toList();
    }

    public Post mapFromPostEntity(User user, PostEntity entity, boolean isLiked) {
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
                isLiked
        );
    }

    public Comment mapFromCommentEntity(CommentEntity entity) {
        var user = mapFromUserEntity(entity.getUser());
        return new Comment(entity.getId(), user, entity.getText(), entity.getCreatedAt());
    }

    public User mapFromUserEntity(UserEntity entity) {
        var avatarName = entity.getAvatar() != null ? entity.getAvatar().getName() : null;
        return new User(entity.getId(), entity.getUsername(), avatarName);
    }
}
