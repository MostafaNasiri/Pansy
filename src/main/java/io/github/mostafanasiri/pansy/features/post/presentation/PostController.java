package io.github.mostafanasiri.pansy.features.post.presentation;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.common.BaseController;
import io.github.mostafanasiri.pansy.features.file.FileUtils;
import io.github.mostafanasiri.pansy.features.post.domain.PostService;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.post.presentation.request.AddCommentRequest;
import io.github.mostafanasiri.pansy.features.post.presentation.request.CreatePostRequest;
import io.github.mostafanasiri.pansy.features.post.presentation.response.CommentResponse;
import io.github.mostafanasiri.pansy.features.post.presentation.response.PostResponse;
import io.github.mostafanasiri.pansy.features.post.presentation.response.UserResponse;
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
        var author = new User(getCurrentUser().getId());
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
        var userResponse = mapFromUserModel(post.user());

        var imageUrls = post.images()
                .stream()
                .map((i) -> fileUtils.createFileUrl(i.name()))
                .toList();

        return new PostResponse(
                post.id(),
                userResponse,
                post.caption(),
                imageUrls,
                post.likesCount()
        );
    }

    private UserResponse mapFromUserModel(User user) {
        var avatarUrl = user.avatar() != null ? fileUtils.createFileUrl(user.avatar()) : null;

        return new UserResponse(
                user.id(),
                user.name(),
                avatarUrl
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

    @GetMapping("/posts/{post_id}/comments")
    @Operation(summary = "Returns comments of the specified post id")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable(name = "post_id") int postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        var comments = service.getComments(postId, page, size);

        var result = comments.stream()
                .map(this::mapFromCommentModel)
                .toList();

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }

    @PostMapping("/posts/{post_id}/comments")
    @Operation(summary = "Adds a comment for the specified post id")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable(name = "post_id") int postId,
            @Valid @RequestBody AddCommentRequest request
    ) {
        var comment = new Comment(new User(getCurrentUser().getId()), request.text());
        var result = mapFromCommentModel(service.addComment(postId, comment));

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.CREATED);
    }

    private CommentResponse mapFromCommentModel(Comment comment) {
        return new CommentResponse(
                comment.id(),
                mapFromUserModel(comment.user()),
                comment.text(),
                comment.createdAt()
        );
    }

    @DeleteMapping("/posts/{post_id}/comments/{comment_id}")
    @Operation(summary = "Removes a comment from the specified post id")
    public ResponseEntity<ApiResponse<Boolean>> deleteComment(
            @PathVariable(name = "post_id") int postId,
            @PathVariable(name = "comment_id") int commentId
    ) {
        service.deleteComment(getCurrentUser().getId(), postId, commentId);
        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.OK);
    }
}
