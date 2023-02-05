package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.NotificationUser;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.LikeEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.LikeJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.user.domain.UserDomainMapper;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LikeService extends BaseService {
    @Autowired
    private PostService postService;
    @Autowired
    private LikeJpaRepository likeJpaRepository;
    @Autowired
    private PostJpaRepository postJpaRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserDomainMapper userDomainMapper;

    public List<User> getLikes(int postId, int page, int size) {
        var post = getPostEntity(postId);

        var pageRequest = PageRequest.of(page, size);
        var likes = likeJpaRepository.getLikes(post, pageRequest);

        return likes.stream()
                .map(like -> userDomainMapper.userEntityToUser(like.getUser()))
                .toList();
    }

    @Transactional
    public void likePost(int postId) {
        var authenticatedUserHasAlreadyLikedThePost = likeJpaRepository.findByUserIdAndPostId(
                getAuthenticatedUserId(),
                postId
        ).isPresent();

        if (!authenticatedUserHasAlreadyLikedThePost) {
            var userEntity = getAuthenticatedUser();
            var postEntity = getPostEntity(postId);

            var like = new LikeEntity(userEntity, postEntity);
            likeJpaRepository.save(like);

            updatePostLikeCount(postId, postEntity);

            var notification = new LikeNotification(
                    new NotificationUser(getAuthenticatedUserId()),
                    new NotificationUser(postEntity.getUser().getId()),
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

        var like = likeJpaRepository.findByUserIdAndPostId(userId, postId);
        var userHasLikedThePost = like.isPresent();

        if (userHasLikedThePost) {
            likeJpaRepository.delete(like.get());

            var postEntity = getPostEntity(postId);
            updatePostLikeCount(postId, postEntity);

            notificationService.deleteLikeNotification(userId, postId);
        }
    }

    private void updatePostLikeCount(int postId, PostEntity postEntity) {
        var likeCount = likeJpaRepository.getPostLikeCount(postEntity);
        postService.updatePostLikeCount(postId, likeCount);
    }

    private PostEntity getPostEntity(int postId) {
        return postJpaRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }
}
