package io.github.mostafanasiri.pansy;

import io.github.mostafanasiri.pansy.app.domain.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.app.domain.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.app.domain.model.File;
import io.github.mostafanasiri.pansy.app.domain.model.Image;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.service.UserService;
import io.github.mostafanasiri.pansy.app.presentation.FileUtils;
import io.github.mostafanasiri.pansy.app.presentation.request.FollowUnfollowUserRequest;
import io.github.mostafanasiri.pansy.app.presentation.request.UpdateUserRequest;
import io.github.mostafanasiri.pansy.app.presentation.response.FullUserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private FileUtils fileUtils;

    @Test
    public void getUser_successful_returnsUserData() throws Exception {
        // Arrange
        var userId = 13;

        var user = new User(1, "Full Name", "username", "", new Image(0, "x.jpg"), "bio", 10, 100, 200);

        when(userService.getUser(userId))
                .thenReturn(user);

        var expectedResponse = createSuccessApiResponse(
                new FullUserResponse(
                        user.id(),
                        user.fullName(),
                        user.username(),
                        user.bio(),
                        fileUtils.createFileUrl(user.avatar().name()),
                        user.postCount(),
                        user.followerCount(),
                        user.followingCount()
                )
        );

        // Act
        var result = mockMvc.perform(
                get("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isOk());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void getUser_invalidUserId_returnsError() throws Exception {
        // Arrange
        var userId = 13;

        var exception = new EntityNotFoundException(User.class, userId);

        when(userService.getUser(userId))
                .thenThrow(exception);

        var expectedResponse = createFailApiResponse(exception.getMessage());

        // Act
        var result = mockMvc.perform(
                get("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isNotFound());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void updateUser_invalidFileId_returnsError() throws Exception {
        // Arrange
        var userId = 13;
        var fileId = 1;

        var requestDto = new UpdateUserRequest("full", "", fileId);

        var exception = new EntityNotFoundException(File.class, fileId);
        when(userService.updateUser(any()))
                .thenThrow(exception);

        var expectedResponse = createFailApiResponse(exception.getMessage());

        // Act
        var result = mockMvc.perform(
                put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isNotFound());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void updateUser_emptyFullName_returnsError() throws Exception {
        // Arrange
        var requestDto = new UpdateUserRequest("", "", null);

        var body = new HashMap<String, String>();
        body.put("fullName", "size must be between 1 and 255");

        var expectedResponse = createFailApiResponse(body);

        // Act
        var result = mockMvc.perform(
                put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isUnprocessableEntity());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void updateUser_successful_returnsUpdatedUserData() throws Exception {
        // Arrange
        var userId = 13;
        var fileId = 1;

        var requestDto = new UpdateUserRequest("full", "bio", fileId);

        var user = new User(userId, requestDto.getFullName(), new Image(fileId), requestDto.getBio());
        var updatedUser = new User(
                userId,
                requestDto.getFullName(),
                "username",
                "pass",
                new Image(fileId),
                requestDto.getBio(),
                1,
                2,
                3
        );
        when(userService.updateUser(user))
                .thenReturn(updatedUser);

        var expectedResponse = createSuccessApiResponse(
                new FullUserResponse(
                        updatedUser.id(),
                        updatedUser.fullName(),
                        updatedUser.username(),
                        updatedUser.bio(),
                        fileUtils.createFileUrl(updatedUser.avatar().name()),
                        updatedUser.postCount(),
                        updatedUser.followerCount(),
                        updatedUser.followingCount()
                )
        );

        // Act
        var result = mockMvc.perform(
                put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isOk());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void followUser_successful_returnsTrue() throws Exception {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 13;

        var requestDto = new FollowUnfollowUserRequest(targetUserId);
        var expectedResponse = createSuccessApiResponse(true);

        // Act
        var result = mockMvc.perform(
                post("/users/" + sourceUserId + "/following")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isCreated());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void followUser_invalidTargetUserId_returnsError() throws Exception {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 13;

        var requestDto = new FollowUnfollowUserRequest(targetUserId);

        var exception = new EntityNotFoundException(User.class, targetUserId);

        doThrow(exception)
                .when(userService)
                .followUser(sourceUserId, targetUserId);

        var expectedResponse = createFailApiResponse(exception.getMessage());

        // Act
        var result = mockMvc.perform(
                post("/users/" + sourceUserId + "/following")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isNotFound());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void followUser_targetUserIdSameAsSourceUserId_returnsError() throws Exception {
        // Arrange
        var sourceUserId = 13;
        var targetUserId = sourceUserId;

        var requestDto = new FollowUnfollowUserRequest(targetUserId);

        doThrow(new InvalidInputException("A user can't follow him/herself!"))
                .when(userService)
                .followUser(sourceUserId, targetUserId);

        var expectedResponse = createFailApiResponse("A user can't follow him/herself!");

        // Act
        var result = mockMvc.perform(
                post("/users/" + sourceUserId + "/following")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isUnprocessableEntity());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void unfollowUser_successful_returnsTrue() throws Exception {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 13;

        var requestDto = new FollowUnfollowUserRequest(targetUserId);
        var expectedResponse = createSuccessApiResponse(true);

        // Act
        var result = mockMvc.perform(
                delete("/users/" + sourceUserId + "/following")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isOk());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void unfollowUser_invalidTargetUserId_returnsError() throws Exception {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 13;

        var requestDto = new FollowUnfollowUserRequest(targetUserId);

        var exception = new EntityNotFoundException(User.class, targetUserId);

        doThrow(exception)
                .when(userService)
                .unfollowUser(sourceUserId, targetUserId);

        var expectedResponse = createFailApiResponse(exception.getMessage());

        // Act
        var result = mockMvc.perform(
                delete("/users/" + sourceUserId + "/following")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isNotFound());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void unfollowUser_targetUserIdSameAsSourceUserId_returnsError() throws Exception {
        // Arrange
        var sourceUserId = 13;
        var targetUserId = sourceUserId;

        var requestDto = new FollowUnfollowUserRequest(targetUserId);

        doThrow(new InvalidInputException("A user can't unfollow him/herself!"))
                .when(userService)
                .unfollowUser(sourceUserId, targetUserId);

        var expectedResponse = createFailApiResponse("A user can't unfollow him/herself!");

        // Act
        var result = mockMvc.perform(
                delete("/users/" + sourceUserId + "/following")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isUnprocessableEntity());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }
}
