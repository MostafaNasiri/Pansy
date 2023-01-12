package io.github.mostafanasiri.pansy.features.post.domain;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.LikeEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.LikeRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.UserService;
import io.github.mostafanasiri.pansy.features.user.entity.UserEntity;
import io.github.mostafanasiri.pansy.features.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    public Comment addComment(int postId, Comment comment) {
        var user = getUserEntity(comment.user().id());
        var post = getPostEntity(postId);

        var commentEntity = new CommentEntity(user, post, comment.text());

        return mapFromCommentEntity(commentEntity);
    }

    public void likePost(int userId, int postId) {
        var user = getUserEntity(userId);
        var post = getPostEntity(postId);

        var userHasAlreadyLikedThePost = likeRepository.findByUserIdAndPostId(userId, postId).isPresent();
        if (!userHasAlreadyLikedThePost) {
            var like = new LikeEntity(user, post);
            likeRepository.save(like);
        }
    }

    public void unlikePost(int userId, int postId) {
        var likeEntity = likeRepository.findByUserIdAndPostId(userId, postId);

        var userHasLikedThePost = likeEntity.isPresent();
        if (userHasLikedThePost) {
            likeRepository.delete(likeEntity.get());
        }
    }

    public void deletePost(int userId, int postId) {
        var post = getPostEntity(postId);

        if (post.getUser().getId() != userId) {
            throw new InvalidInputException("The post does not belong to the specified user.");
        }

        postRepository.delete(post);
    }

    public List<Post> getUserPosts(int userId, int page, int size) {
        var userEntity = userService.getUser(userId);

        var pageRequest = PageRequest.of(page, size);
        var result = postRepository.findByAuthorOrderByCreatedAtDesc(userEntity, pageRequest);

        return result.stream()
                .map(pe -> {
                    var likesCount = (int) likeRepository.countByPostId(pe.getId());
                    return mapFromPostEntity(pe, likesCount);
                })
                .toList();
    }

    public Post createPost(Post input) {
        var userEntity = userService.getUser(input.user().id());

        var fileEntities = fileService.getFiles(
                input.images()
                        .stream()
                        .map((i) -> i.id())
                        .collect(Collectors.toSet())
        );

        // Make sure that files are not already attached to any posts
        var fileIds = fileEntities.stream().map(f -> f.getId()).toList();
        var fileIdsThatAreAlreadyAttachedToAPost = postRepository.getFileIdsThatAreAlreadyAttachedToAPost(fileIds);
        if (!fileIdsThatAreAlreadyAttachedToAPost.isEmpty()) {
            throw new InvalidInputException(
                    String.format(
                            "File with id %s is already attached to a post.",
                            fileIdsThatAreAlreadyAttachedToAPost.get(0)
                    )
            );
        }

        var postEntity = new PostEntity(userEntity, input.caption(), fileEntities);
        postEntity = postRepository.save(postEntity);
        var likesCount = (int) likeRepository.countByPostId(postEntity.getId());

        return mapFromPostEntity(postEntity, likesCount);
    }

    private PostEntity getPostEntity(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }

    private UserEntity getUserEntity(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserEntity.class, userId));
    }

    private Post mapFromPostEntity(PostEntity entity, int likesCount) {
        var user = mapFromUserEntity(entity.getUser());

        var images = entity.getImages()
                .stream()
                .map((i) -> new Image(i.getId(), i.getName()))
                .toList();

        return new Post(entity.getId(), user, entity.getCaption(), images, likesCount);
    }

    private Comment mapFromCommentEntity(CommentEntity entity) {
        var user = mapFromUserEntity(entity.getUser());

        return new Comment(entity.getId(), user, entity.getText());
    }

    private User mapFromUserEntity(UserEntity entity) {
        var avatarUrl = entity.getAvatar() != null ? entity.getAvatar().getName() : null;

        return new User(
                entity.getId(),
                entity.getFullName(),
                avatarUrl
        );
    }
}
