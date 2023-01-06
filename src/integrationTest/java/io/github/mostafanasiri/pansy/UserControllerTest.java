package io.github.mostafanasiri.pansy;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.features.file.File;
import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.file.FileUtils;
import io.github.mostafanasiri.pansy.features.user.UserController;
import io.github.mostafanasiri.pansy.features.user.UserService;
import io.github.mostafanasiri.pansy.features.user.dto.*;
import io.github.mostafanasiri.pansy.features.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController controller;

    @MockBean
    private UserService userService;

    @MockBean
    private FileService fileService;

    @Autowired
    private FileUtils fileUtils;

    @Test
    public void getUser_successful_returnsUserData() throws Exception {
        // Arrange
        var userId = 13;

        var entity = new User("test", "user", "pass");
        entity.setId(userId);

        var avatar = new File("x");
        entity.setAvatar(avatar);

        when(userService.getUser(userId))
                .thenReturn(entity);

        var expectedResponse = createSuccessApiResponse(
                new GetUserResponse(
                        entity.getId(),
                        entity.getFullName(),
                        entity.getUsername(),
                        entity.getBio(),
                        fileUtils.createFileUrl(avatar)
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

        when(userService.getUser(userId))
                .thenReturn(new User());

        var exception = new EntityNotFoundException(File.class, fileId);
        when(fileService.getFile(fileId))
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

        var user = new User();
        user.setId(userId);
        user.setFullName(requestDto.getFullName());
        user.setBio(requestDto.getBio());

        when(userService.getUser(userId))
                .thenReturn(user);
        when(userService.updateUser(user))
                .thenReturn(user);

        var file = new File("x");
        when(fileService.getFile(fileId))
                .thenReturn(file);

        var expectedResponse = createSuccessApiResponse(
                new UpdateUserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getBio(),
                        fileUtils.createFileUrl(file)
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

        when(userService.getUser(targetUserId))
                .thenThrow(exception);

        doCallRealMethod()
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

        doCallRealMethod()
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

        when(userService.getUser(targetUserId))
                .thenThrow(exception);

        doCallRealMethod()
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

        doCallRealMethod()
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

    @Test
    public void getFollowers_invalidUserId_returnsError() throws Exception {
        // Arrange
        var userId = 1;
        var exception = new EntityNotFoundException(User.class, userId);

        when(userService.getUser(userId))
                .thenThrow(exception);

        when(userService.getFollowers(userId))
                .thenCallRealMethod();

        var expectedResponse = createFailApiResponse(exception.getMessage());

        // Act
        var result = mockMvc.perform(
                get("/users/" + userId + "/followers")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isNotFound());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void getFollowers_validInput_returnsData() throws Exception {
        // Arrange
        var userId = 1;

        var users = new ArrayList<User>();
        users.add(new User("name", "username", "password"));

        when(userService.getFollowers(userId))
                .thenReturn(users);

        var expectedResponse = createSuccessApiResponse(new GetFollowersFollowingResponse(
                users.stream().map(
                        (u) -> new GetFollowersFollowingResponse.Item(u.getId(), u.getFullName(), null)
                ).toList()
        ));

        // Act
        var result = mockMvc.perform(
                get("/users/" + userId + "/followers")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isOk());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void getFollowing_invalidUserId_returnsError() throws Exception {
        // Arrange
        var userId = 1;
        var exception = new EntityNotFoundException(User.class, userId);

        when(userService.getUser(userId))
                .thenThrow(exception);

        when(userService.getFollowing(userId))
                .thenCallRealMethod();

        var expectedResponse = createFailApiResponse(exception.getMessage());

        // Act
        var result = mockMvc.perform(
                get("/users/" + userId + "/following")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isNotFound());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void getFollowing_validInput_returnsData() throws Exception {
        // Arrange
        var userId = 1;

        var users = new ArrayList<User>();
        users.add(new User("name", "username", "password"));

        when(userService.getFollowing(userId))
                .thenReturn(users);

        var expectedResponse = createSuccessApiResponse(new GetFollowersFollowingResponse(
                users.stream().map(
                        (u) -> new GetFollowersFollowingResponse.Item(u.getId(), u.getFullName(), null)
                ).toList()
        ));

        // Act
        var result = mockMvc.perform(
                get("/users/" + userId + "/following")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isOk());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }
}
