package io.github.mostafanasiri.pansy.features.user.presentation;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.features.user.domain.UserService;
import io.github.mostafanasiri.pansy.features.user.domain.model.Image;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.presentation.request.FollowUnfollowUserRequest;
import io.github.mostafanasiri.pansy.features.user.presentation.request.UpdateUserRequest;
import io.github.mostafanasiri.pansy.features.user.presentation.response.FollowersFollowingResponse;
import io.github.mostafanasiri.pansy.features.user.presentation.response.UserResponse;
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

@Tag(name = "User")
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private ResponseMapper responseMapper;

    @GetMapping("/{user_id}")
    @Operation(summary = "Returns a user's public data")
    public ResponseEntity<ApiResponse<UserResponse>> getPublicUserData(@PathVariable(name = "user_id") int userId) {
        var user = userService.getPublicUserData(userId);
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
        var user = new User(userId, request.getFullName(), avatarImage, request.getBio());

        var updatedUser = userService.updateUser(user);
        var response = responseMapper.fromUserModel(updatedUser);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
    }

    @GetMapping("/{user_id}/followers")
    @Operation(summary = "Returns a list of users who are followers of the specified user id")
    public ResponseEntity<ApiResponse<List<FollowersFollowingResponse>>> getFollowers(
            @PathVariable(name = "user_id") int userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "30") @Max(50) int size
    ) {
        var followers = userService.getFollowers(userId, page, size);
        var response = responseMapper.fromUserModels(followers);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
    }

    @GetMapping("/{user_id}/following")
    @Operation(summary = "Returns a list of users the specified user id is following")
    public ResponseEntity<ApiResponse<List<FollowersFollowingResponse>>> getFollowing(
            @PathVariable(name = "user_id") int userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "30") @Max(50) int size
    ) {
        var following = userService.getFollowing(userId, page, size);
        var response = responseMapper.fromUserModels(following);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
    }

    @PostMapping("/{user_id}/following")
    @Operation(summary = "Allows a user id to follow another user")
    public ResponseEntity<ApiResponse<Boolean>> followUser(
            @PathVariable(name = "user_id") int userId,
            @Valid @RequestBody FollowUnfollowUserRequest request
    ) {
        userService.followUser(userId, request.targetUserId());

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.CREATED);
    }

    @DeleteMapping("/{user_id}/following")
    @Operation(summary = "Allows a user id to unfollow another user")
    public ResponseEntity<ApiResponse<Boolean>> unfollowUser(
            @PathVariable(name = "user_id") int userId,
            @Valid @RequestBody FollowUnfollowUserRequest request
    ) {
        userService.unfollowUser(userId, request.targetUserId());

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.OK);
    }
}
