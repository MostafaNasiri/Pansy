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
            List<PostEntity> postEntities,
            List<Integer> likedPostIds
    ) {
        return postEntities.stream()
                .map(pe -> {
                    var isLiked = likedPostIds.contains(pe.getId());
                    return postEntityToPost(postAuthor, pe, isLiked);
                })
                .toList();
    }

    public Post postEntityToPost(User postAuthor, PostEntity entity, boolean isLiked) { // TODO: remove isLiked
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
                isLiked,
                entity.getCreatedAt()
        );
    }

    // This method is used when we want to map posts of different users
    public List<Post> postEntitiesToPosts(List<PostEntity> postEntities, List<Integer> likedPostIds) {
        return postEntities.stream()
                .map(pe -> {
                    var isLiked = likedPostIds.contains(pe.getId());
                    return postEntityToPost(pe, isLiked);
                })
                .toList();
    }

    public Post postEntityToPost(PostEntity entity, boolean isLiked) { // TODO: remove isLiked
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
                isLiked,
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
        var imageUrls = post.images().stream().map(Image::name).toList();

        return new PostRedis(
                post.id(),
                userRedis,
                post.caption(),
                imageUrls,
                post.likeCount(),
                post.commentCount(),
                post.createdAt()
        );
    }

    public FeedEntity.FeedItem postToFeedItem(Post post) {
        return new FeedEntity.FeedItem(post.user().id(), post.id());
    }

    public Comment commentEntityToComment(CommentEntity entity) {
        var user = userDomainMapper.userEntityToUser(entity.getUser());
        return new Comment(entity.getId(), user, entity.getText(), entity.getCreatedAt());
    }
}
