package io.github.mostafanasiri.pansy.features.post.presentation;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.service.CommentService;
import io.github.mostafanasiri.pansy.features.post.domain.service.LikeService;
import io.github.mostafanasiri.pansy.features.post.domain.service.PostService;
import io.github.mostafanasiri.pansy.features.post.presentation.request.AddCommentRequest;
import io.github.mostafanasiri.pansy.features.post.presentation.request.CreateEditPostRequest;
import io.github.mostafanasiri.pansy.features.post.presentation.response.CommentResponse;
import io.github.mostafanasiri.pansy.features.post.presentation.response.PostResponse;
import io.github.mostafanasiri.pansy.features.post.presentation.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post")
@RestController
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ResponseMapper mapper;

    @GetMapping("/users/{user_id}/posts")
    @Operation(summary = "Returns a user's posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPosts(
            @PathVariable(name = "user_id") int userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "30") @Max(50) int size
    ) {
        var posts = postService.getUserPosts(userId, page, size);
        var result = posts.stream().map(mapper::mapFromPostModel).toList();

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }

    @PostMapping("/posts")
    @Operation(summary = "Creates a new post for the authenticated user")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody CreateEditPostRequest request) {
        var post = new Post(
                request.caption(),
                request.imageIds()
                        .stream()
                        .map(Image::new)
                        .toList()
        );
        var result = mapper.mapFromPostModel(postService.createPost(post));

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.CREATED);
    }

    @PutMapping("/posts/{post_id}")
    @Operation(summary = "Edits a post")
    public ResponseEntity<ApiResponse<PostResponse>> editPost(
            @PathVariable(name = "post_id") int postId,
            @Valid @RequestBody CreateEditPostRequest request
    ) {
        var post = new Post(
                postId,
                request.caption(),
                request.imageIds()
                        .stream()
                        .map(Image::new)
                        .toList()
        );
        var result = mapper.mapFromPostModel(postService.updatePost(post));

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }

    @DeleteMapping("/posts/{post_id}")
    @Operation(summary = "Deletes a post")
    public ResponseEntity<ApiResponse<Boolean>> deletePost(@PathVariable(name = "post_id") int postId) {
        postService.deletePost(postId);
        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.OK);
    }

    @PostMapping("/posts/{post_id}/likes")
    @Operation(summary = "Likes the specified post by the authenticated user")
    public ResponseEntity<ApiResponse<Boolean>> likePost(@PathVariable(name = "post_id") int postId) {
        likeService.likePost(postId);
        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.CREATED);
    }

    @GetMapping("/posts/{post_id}/likes")
    @Operation(summary = "Returns a list of users who liked the specified post id")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getPostLikes(
            @PathVariable(name = "post_id") int postId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "30") @Max(50) int size
    ) {
        var users = likeService.getLikes(postId, page, size);

        var result = users.stream()
                .map(mapper::mapFromUserModel)
                .toList();

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }

    @DeleteMapping("/posts/{post_id}/likes/{user_id}")
    @Operation(summary = "Unlikes a post that has already been liked by the authenticated user")
    public ResponseEntity<ApiResponse<Boolean>> unlikePost(
            @PathVariable(name = "post_id") int postId,
            @PathVariable(name = "user_id") int userId
    ) {
        likeService.unlikePost(userId, postId);
        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.CREATED);
    }

    @GetMapping("/posts/{post_id}/comments")
    @Operation(summary = "Returns comments of the specified post id")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable(name = "post_id") int postId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "30") @Max(50) int size
    ) {
        var comments = commentService.getComments(postId, page, size);

        var result = comments.stream()
                .map(mapper::mapFromCommentModel)
                .toList();

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }

    @PostMapping("/posts/{post_id}/comments")
    @Operation(summary = "Adds a comment for the specified post id")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable(name = "post_id") int postId,
            @Valid @RequestBody AddCommentRequest request
    ) {
        var comment = new Comment(request.text());
        var result = mapper.mapFromCommentModel(commentService.addComment(postId, comment));

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.CREATED);
    }

    @DeleteMapping("/posts/{post_id}/comments/{comment_id}")
    @Operation(summary = "Removes a comment from the specified post id")
    public ResponseEntity<ApiResponse<Boolean>> deleteComment(
            @PathVariable(name = "post_id") int postId,
            @PathVariable(name = "comment_id") int commentId
    ) {
        commentService.deleteComment(postId, commentId);
        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.OK);
    }
}
