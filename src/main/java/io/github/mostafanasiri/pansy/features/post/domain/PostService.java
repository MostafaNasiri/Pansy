package io.github.mostafanasiri.pansy.features.post.domain;

import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Author;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

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

        return mapFromPostEntity(postEntity);
    }

    private Post mapFromPostEntity(PostEntity entity) {
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

        return new Post(entity.getId(), author, entity.getCaption(), images);
    }
}
