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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post")
@RestController
public class PostController extends BaseController {
    @Autowired
    private PostService service;

    @Autowired
    private FileUtils fileUtils;

    @GetMapping("/users/{user_id}/posts")
    @Operation(summary = "Returns a user's posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPosts(
            @PathVariable(name = "user_id") int userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        var posts = service.getUserPosts(userId, page, size);
        var result = posts.stream().map(this::mapFromPostModel).toList();

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }

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
        var avatarUrl = post.author().avatar() != null ? fileUtils.createFileUrl(post.author().avatar()) : null;
        var authorResponse = new PostAuthorResponse(
                post.author().id(),
                post.author().name(),
                avatarUrl
        );

        var imageUrls = post.images()
                .stream()
                .map((i) -> fileUtils.createFileUrl(i.name()))
                .toList();

        return new PostResponse(
                post.id(),
                authorResponse,
                post.caption(),
                imageUrls,
                post.likesCount()
        );
    }

    // TODO - [PUT] /posts/{post_id} - Edits a post

    @DeleteMapping("/posts/{post_id}")
    @Operation(summary = "Deletes a post")
    public ResponseEntity<ApiResponse<Boolean>> deletePost(@PathVariable(name = "post_id") int postId) {
        service.deletePost(getCurrentUser().getId(), postId);
        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.OK);
    }

    @PostMapping("/posts/{post_id}/likes")
    @Operation(summary = "Likes the specified post by the authorized user")
    public ResponseEntity<ApiResponse<Boolean>> likePost(@PathVariable(name = "post_id") int postId) {
        service.likePost(getCurrentUser().getId(), postId);
        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.CREATED);
    }

    // TODO - [GET] /posts/{post_id}/likes - Returns a list of users who liked the specified post id

    @DeleteMapping("/posts/{post_id}/likes/{user_id}")
    @Operation(summary = "Unlikes a post that has already been liked by the authorized user")
    public ResponseEntity<ApiResponse<Boolean>> unlikePost(@PathVariable(name = "post_id") int postId) {
        service.unlikePost(getCurrentUser().getId(), postId);
        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.CREATED);
    }

    // TODO - [GET] /posts/{post_id}/comments - Returns comments of the specified post id

    // TODO - [POST] /posts/{post_id}/comments - Adds a comment for the specified post id

    // TODO - [DELETE] /posts/{post_id}/comments/{comment_id} - Removes a comment from the specified post id
}
