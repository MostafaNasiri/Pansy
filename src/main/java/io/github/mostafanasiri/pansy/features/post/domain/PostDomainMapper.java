package io.github.mostafanasiri.pansy.features.post.domain;

import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.FeedEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.redis.PostRedis;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.user.data.entity.redis.UserRedis;
import io.github.mostafanasiri.pansy.features.user.domain.UserDomainMapper;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostDomainMapper {
    @Autowired
    private UserDomainMapper userDomainMapper;

    // This method is used when we want to map posts of a single user
    public List<Post> postEntitiesToPosts(
            User postAuthor,
            List<PostEntity> postEntities
    ) {
        return postEntities.stream()
                .map(pe -> postEntityToPost(postAuthor, pe))
                .toList();
    }

    public Post postEntityToPost(User postAuthor, PostEntity entity) {
        var images = entity.getImages()
                .stream()
                .map((i) -> new Image(i.getId(), i.getName()))
                .toList();

        return new Post(
                entity.getId(),
                postAuthor,
                entity.getCaption(),
                images,
                entity.getLikeCount(),
                entity.getCommentCount(),
                entity.getCreatedAt()
        );
    }

    // This method is used when we want to map posts of different users
    public List<Post> postEntitiesToPosts(List<PostEntity> postEntities) {
        return postEntities.stream()
                .map(this::postEntityToPost)
                .toList();
    }

    public Post postEntityToPost(PostEntity entity) {
        var user = userDomainMapper.userEntityToUser(entity.getUser());

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
                entity.getCreatedAt()
        );
    }

    public List<Post> postsRedisToPosts(List<PostRedis> postRedis) {
        return postRedis.stream()
                .map(this::postRedisToPost)
                .toList();
    }

    public Post postRedisToPost(PostRedis postRedis) {
        var user = userDomainMapper.userRedisToUser(postRedis.user());
        var images = postRedis.imageNames()
                .stream()
                .map(n -> new Image(0, n))
                .toList();

        return new Post(
                postRedis.id(),
                user,
                postRedis.caption(),
                images,
                postRedis.likeCount(),
                postRedis.commentCount(),
                postRedis.createdAt()
        );
    }

    public PostRedis postToPostRedis(UserRedis userRedis, Post post) {
        var imageUrls = post.getImages().stream().map(Image::name).toList();

        return new PostRedis(
                post.getId(),
                userRedis,
                post.getCaption(),
                imageUrls,
                post.getLikeCount(),
                post.getCommentCount(),
                post.getCreatedAt()
        );
    }

    public FeedEntity.FeedItem postToFeedItem(Post post) {
        return new FeedEntity.FeedItem(post.getUser().id(), post.getId());
    }

    public Comment commentEntityToComment(CommentEntity entity) {
        var user = userDomainMapper.userEntityToUser(entity.getUser());
        return new Comment(entity.getId(), user, entity.getText(), entity.getCreatedAt());
    }
}
