package io.github.mostafanasiri.pansy.features.post.domain;

import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Author;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                        .toList()
        );

        var postEntity = new PostEntity(userEntity, input.caption(), fileEntities);
        postEntity = postRepository.save(postEntity);

        var avatarUrl = postEntity.getAuthor().getAvatar() != null ? postEntity.getAuthor().getAvatar().getName() : null;
        var author = new Author(
                postEntity.getAuthor().getId(),
                postEntity.getAuthor().getFullName(),
                avatarUrl
        );
        var images = postEntity.getImages()
                .stream()
                .map((i) -> new Image(i.getId(), i.getName()))
                .toList();
        var post = new Post(postEntity.getId(), author, postEntity.getCaption(), images);

        return post;
    }
}
