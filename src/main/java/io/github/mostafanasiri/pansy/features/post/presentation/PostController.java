package io.github.mostafanasiri.pansy.features.post.presentation;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.common.BaseController;
import io.github.mostafanasiri.pansy.features.file.FileUtils;
import io.github.mostafanasiri.pansy.features.post.domain.PostService;
import io.github.mostafanasiri.pansy.features.post.domain.model.Author;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.presentation.request.CreatePostRequest;
import io.github.mostafanasiri.pansy.features.post.presentation.response.PostAuthorResponse;
import io.github.mostafanasiri.pansy.features.post.presentation.response.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Post")
@RestController
public class PostController extends BaseController {
    @Autowired
    private PostService service;

    @Autowired
    private FileUtils fileUtils;

    // TODO - [GET] /users/{user_id}/posts - Returns a user's posts

    @PostMapping("/posts")
    @Operation(summary = "Creates a new post for the authorized user")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody CreatePostRequest request) {
        var author = new Author(getCurrentUser().getId());
        var post = new Post(
                author,
                request.caption(),
                request.imageIds()
                        .stream()
                        .map(Image::new)
                        .toList()
        );

        var result = mapFromPostModel(service.createPost(post));

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.CREATED);
    }

    private PostResponse mapFromPostModel(Post post) {
        var authorResponse = new PostAuthorResponse(
                post.author().id(),
                post.author().name(),
                fileUtils.createFileUrl(post.author().avatar())
        );

        var imageUrls = post.images()
                .stream()
                .map((i) -> i.url())
                .toList();

        return new PostResponse(
                post.id(),
                authorResponse,
                post.caption(),
                imageUrls
        );
    }

    // TODO - [PUT] /posts/{post_id} - Edits a post

    // TODO - [DELETE] /posts/{post_id} - Deletes a post


    // TODO - [POST] /posts/{post_id}/likes - Likes the specified post by the authorized user

    // TODO - [GET] /posts/{post_id}/likes - Returns a list of users who liked the specified post id

    // TODO - [DELETE] /posts/{post_id}/likes/{user_id} - Unlikes a post that has already been liked by the authorized user


    // TODO - [GET] /posts/{post_id}/comments - Returns comments of the specified post id

    // TODO - [POST] /posts/{post_id}/comments - Adds a comment for the specified post id

    // TODO - [DELETE] /posts/{post_id}/comments/{comment_id} - Removes a comment from the specified post id
}
