package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.LikeEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.LikeJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LikeService extends BaseService {
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LikeJpaRepository likeJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private PostJpaRepository postJpaRepository;

    public @NonNull List<User> getPostLikers(int postId, int page, int size) {
        var post = postService.getPost(postId);

        var pageRequest = PageRequest.of(page, size);
        var likerUserIds = likeJpaRepository.getLikerUserIds(post.getId(), pageRequest);

        return userService.getUsers(likerUserIds);
    }

    @Transactional
    public void likePost(int postId) {
        var authenticatedUserHasAlreadyLikedThePost = isPostLikedByUser(postId, getAuthenticatedUserId());

        if (!authenticatedUserHasAlreadyLikedThePost) {
            var userEntity = userJpaRepository.getReferenceById(getAuthenticatedUserId());
            var post = postService.getPost(postId);

            var like = new LikeEntity(userEntity, postJpaRepository.getReferenceById(post.getId()));
            likeJpaRepository.save(like);

            updatePostLikeCount(post.getId());

            var notification = new LikeNotification(
                    new User(getAuthenticatedUserId()),
                    new User(post.getUser().id()),
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

        var post = postService.getPost(postId);

        var like = likeJpaRepository.findByUserIdAndPostId(userId, post.getId());
        var userHasLikedThePost = like.isPresent();

        if (userHasLikedThePost) {
            likeJpaRepository.delete(like.get());
            updatePostLikeCount(post.getId());
            notificationService.deleteLikeNotification(userId, postId);
        }
    }

    public boolean isPostLikedByUser(int postId, int userId) {
        return likeJpaRepository.findByUserIdAndPostId(userId, postId)
                .isPresent();
    }

    private void updatePostLikeCount(int postId) {
        var likeCount = likeJpaRepository.getPostLikeCount(postId);
        postService.updatePostLikeCount(postId, likeCount);
    }
}
