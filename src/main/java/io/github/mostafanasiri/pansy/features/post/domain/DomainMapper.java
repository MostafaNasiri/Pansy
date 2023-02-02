package io.github.mostafanasiri.pansy.features.post.domain;

import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.redis.PostRedis;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.redis.UserRedis;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("postFeatureModelMapper")
public class DomainMapper {
    public List<Post> postEntitiesToPosts(
            UserEntity userEntity,
            List<PostEntity> postEntities,
            List<Integer> likedPostIds
    ) {
        var user = userEntityToUser(userEntity);

        return postEntities.stream()
                .map(pe -> {
                    var isLiked = likedPostIds.contains(pe.getId());
                    return postEntityToPost(user, pe, isLiked);
                })
                .toList();
    }

    public Post postEntityToPost(User user, PostEntity entity, boolean isLiked) {
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
                entity.getCommentCount(),
                isLiked
        );
    }

    public PostRedis postToPostRedis(UserRedis userRedis, Post post) {
        var imageUrls = post.images().stream().map(Image::name).toList();

        return new PostRedis(
                post.id(),
                userRedis,
                post.caption(),
                imageUrls,
                post.likeCount(),
                post.commentCount()
        );
    }

    public Comment commentEntityToComment(CommentEntity entity) {
        var user = userEntityToUser(entity.getUser());
        return new Comment(entity.getId(), user, entity.getText(), entity.getCreatedAt());
    }

    public User userEntityToUser(UserEntity entity) {
        var avatarName = entity.getAvatar() != null ? entity.getAvatar().getName() : null;
        return new User(entity.getId(), entity.getUsername(), avatarName);
    }
}
