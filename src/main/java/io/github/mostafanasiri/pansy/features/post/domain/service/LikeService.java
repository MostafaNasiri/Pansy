package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.NotificationUser;
import io.github.mostafanasiri.pansy.features.post.data.entity.LikeEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.LikeRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.ModelMapper;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class LikeService extends BaseService {
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ModelMapper modelMapper;

    public List<User> getLikes(int postId, int page, int size) {
        var post = getPostEntity(postId);

        var pageRequest = PageRequest.of(page, size);
        var likes = likeRepository.getLikes(post, pageRequest);

        return likes.stream()
                .map(like -> modelMapper.mapFromUserEntity(like.getUser()))
                .toList();
    }

    @Transactional
    public void likePost(int postId) {
        var authenticatedUserHasAlreadyLikedThePost = likeRepository.findByUserIdAndPostId(
                getAuthenticatedUserId(),
                postId
        ).isPresent();

        if (!authenticatedUserHasAlreadyLikedThePost) {
            var user = getAuthenticatedUser();
            var post = getPostEntity(postId);

            var like = new LikeEntity(user, post);
            likeRepository.save(like);

            post.incrementLikeCount();
            postRepository.save(post);

            var notification = new LikeNotification(
                    new NotificationUser(getAuthenticatedUserId()),
                    new NotificationUser(post.getUser().getId()),
                    postId
            );
            notificationService.addLikeNotification(notification);
        }
    }

    @Transactional
    public void unlikePost(int userId, int postId) {
        if (getAuthenticatedUserId() != userId) {
            throw new AuthorizationException("Forbidden action");
        }

        var like = likeRepository.findByUserIdAndPostId(userId, postId);

        var userHasLikedThePost = like.isPresent();

        if (userHasLikedThePost) {
            likeRepository.delete(like.get());
            decrementPostLikeCount(postId);
            notificationService.deleteLikeNotification(userId, postId);
        }
    }

    private void decrementPostLikeCount(int postId) {
        var post = getPostEntity(postId);
        post.decrementLikeCount();
        postRepository.save(post);
    }

    private PostEntity getPostEntity(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }
}
