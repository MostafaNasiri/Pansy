package io.github.mostafanasiri.pansy.features.user.presentation;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.common.BaseController;
import io.github.mostafanasiri.pansy.features.user.domain.model.Image;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.domain.service.UserService;
import io.github.mostafanasiri.pansy.features.user.presentation.request.FollowUnfollowUserRequest;
import io.github.mostafanasiri.pansy.features.user.presentation.request.UpdateUserRequest;
import io.github.mostafanasiri.pansy.features.user.presentation.response.GetFollowersFollowingResponse;
import io.github.mostafanasiri.pansy.features.user.presentation.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
public class UserController extends BaseController {
    @Autowired
    private UserService userService;

    @Autowired
    private ResponseMapper responseMapper;

    @GetMapping("/{user_id}")
    @Operation(summary = "Returns a user's public data")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable(name = "user_id") int userId) {
        var user = userService.getUser(userId);

        var response = responseMapper.fromUserModel(user);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
    }

    @PutMapping("/{user_id}")
    @Operation(summary = "Updates a user's data")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable(name = "user_id") int userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        Image avatarImage = null;
        if (request.getAvatarFileId() != null) {
            avatarImage = new Image(request.getAvatarFileId());
        }
        var user = new User(userId, request.getFullName(), avatarImage);

        var updatedUser = userService.updateUser(getCurrentUser().getId(), user);
        var response = responseMapper.fromUserModel(updatedUser);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
    }

    @GetMapping("/{user_id}/followers")
    @Operation(summary = "Returns a list of users who are followers of the specified user id")
    public ResponseEntity<ApiResponse<GetFollowersFollowingResponse>> getFollowers(
            @PathVariable(name = "user_id") int userId
    ) {
        var followers = userService.getFollowers(userId); // TODO: Add pagination
        var response = responseMapper.fromUserModels(followers);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
    }

    @GetMapping("/{user_id}/following")
    @Operation(summary = "Returns a list of users the specified user id is following")
    public ResponseEntity<ApiResponse<GetFollowersFollowingResponse>> getFollowing(
            @PathVariable(name = "user_id") int userId
    ) {
        var following = userService.getFollowing(userId); // TODO: Add pagination
        var response = responseMapper.fromUserModels(following);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
    }

    @PostMapping("/{user_id}/following")
    @Operation(summary = "Allows a user id to follow another user")
    public ResponseEntity<ApiResponse<Boolean>> followUser(
            @PathVariable(name = "user_id") int userId,
            @Valid @RequestBody FollowUnfollowUserRequest request
    ) {
        userService.followUser(
                getCurrentUser().getId(),
                userId,
                request.targetUserId()
        );

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.CREATED);
    }

    @DeleteMapping("/{user_id}/following")
    @Operation(summary = "Allows a user id to unfollow another user")
    public ResponseEntity<ApiResponse<Boolean>> unfollowUser(
            @PathVariable(name = "user_id") int userId,
            @Valid @RequestBody FollowUnfollowUserRequest request
    ) {
        userService.unfollowUser(
                getCurrentUser().getId(),
                userId,
                request.targetUserId()
        );

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.OK);
    }
}
