package io.github.mostafanasiri.pansy.features.post.domain;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.post.data.entity.LikeEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.LikeRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Author;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.user.UserService;
import io.github.mostafanasiri.pansy.features.user.entity.User;
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

    public void likePost(int userId, int postId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
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

        if (post.getAuthor().getId() != userId) {
            throw new InvalidInputException("The post does not belong to the specified user.");
        }

        postRepository.delete(post);
    }

    private PostEntity getPostEntity(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
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
        var userEntity = userService.getUser(input.author().id());

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

    private Post mapFromPostEntity(PostEntity entity, int likesCount) {
        var avatarUrl = entity.getAuthor().getAvatar() != null ? entity.getAuthor().getAvatar().getName() : null;
        var author = new Author(
                entity.getAuthor().getId(),
                entity.getAuthor().getFullName(),
                avatarUrl
        );

        var images = entity.getImages()
                .stream()
                .map((i) -> new Image(i.getId(), i.getName()))
                .toList();

        return new Post(entity.getId(), author, entity.getCaption(), images, likesCount);
    }
}
