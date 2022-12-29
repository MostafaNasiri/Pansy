package io.github.mostafanasiri.pansy.features.user;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.file.FileUtils;
import io.github.mostafanasiri.pansy.features.user.dto.*;
import io.github.mostafanasiri.pansy.features.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileUtils fileUtils;

    @PostMapping("/users")
    @Operation(summary = "Creates a new user")
    public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        var entity = new User();
        entity.setFullName(request.getFullName());
        entity.setUsername(request.getUsername());
        entity.setPassword(request.getPassword());

        var createdUser = userService.createUser(entity);
        var response = new CreateUserResponse(createdUser.getId());

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.CREATED);
    }

    @PutMapping("/users/{user_id}")
    @Operation(summary = "Updates a user's data")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUser(
            @PathVariable(name = "user_id") int userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        var user = userService.getUser(userId);
        user.setFullName(request.getFullName());
        user.setBio(request.getBio());

        if (request.getAvatarFileId() != null) {
            var avatarFile = fileService.getFile(request.getAvatarFileId());
            user.setAvatar(avatarFile);
        }

        var updatedUser = userService.updateUser(user);

        var response = new UpdateUserResponse(
                updatedUser.getId(),
                updatedUser.getFullName(),
                updatedUser.getBio(),
                fileUtils.createFileUrl(updatedUser.getAvatar().getName())
        );

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
    }

    @GetMapping("/users/{user_id}")
    @Operation(summary = "Returns a user's public data")
    public ResponseEntity<ApiResponse<GetUserResponse>> getUser(@PathVariable(name = "user_id") int userId) {
        var entity = userService.getUser(userId);

        var response = new GetUserResponse(
                entity.getId(),
                entity.getFullName(),
                entity.getUsername(),
                entity.getBio(),
                fileUtils.createFileUrl(entity.getAvatar().getName())
        );

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
    }

    // TODO: [GET] /users/me - Returns information about an authorized user.

    // TODO: [GET] /users/{user_id}/following - Returns a list of users the specified user ID is following.

    // TODO: [GET] /users/{user_id}/followers - Returns a list of users who are followers of the specified user ID.

    @PostMapping("/users/{user_id}/following")
    @Operation(summary = "Allows a user ID to follow another user")
    public ResponseEntity<ApiResponse<Boolean>> followUser(
            @PathVariable(name = "user_id") int userId,
            @Valid @RequestBody FollowUnfollowUserRequest request
    ) {
        userService.followUser(userId, request.targetUserId());

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.CREATED);
    }

    @DeleteMapping("/users/{user_id}/following")
    @Operation(summary = "Allows a user ID to unfollow another user")
    public ResponseEntity<ApiResponse<Boolean>> unfollowUser(
            @PathVariable(name = "user_id") int userId,
            @Valid @RequestBody FollowUnfollowUserRequest request
    ) {
        userService.unfollowUser(userId, request.targetUserId());

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, true), HttpStatus.OK);
    }
}
