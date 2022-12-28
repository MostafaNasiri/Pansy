package io.github.mostafanasiri.pansy.controller;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.File;
import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.file.FileUtils;
import io.github.mostafanasiri.pansy.features.user.User;
import io.github.mostafanasiri.pansy.features.user.UserController;
import io.github.mostafanasiri.pansy.features.user.UserService;
import io.github.mostafanasiri.pansy.features.user.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
    public void createUser_successful_returnsCreatedUser() throws Exception {
        // Arrange
        var requestDto = getValidInputForCreateUser();

        var entity = new User(requestDto.getFullName(), requestDto.getUsername(), requestDto.getPassword());

        var createdUserId = 13;
        entity.setId(createdUserId);

        when(userService.createUser(entity))
                .thenReturn(entity);

        var expectedResponse = createSuccessApiResponse(new CreateUserResponse(createdUserId));

        // Act
        var result = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isCreated());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    private CreateUserRequest getValidInputForCreateUser() {
        return new CreateUserRequest("first last", "username", "pass123");
    }

    @Test
    public void createUser_emptyFullName_returnsError() throws Exception {
        // Arrange
        var requestDto = new CreateUserRequest("", "username", "password");

        var body = new HashMap<String, String>();
        body.put("fullName", "size must be between 1 and 255");

        var expectedResponse = createFailApiResponse(body);

        // Act
        var result = mockMvc.perform(
                post("/users")
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
    public void createUser_emptyUsername_returnsError() throws Exception {
        // Arrange
        var requestDto = new CreateUserRequest("full name", "", "password");

        var body = new HashMap<String, String>();
        body.put("username", "Invalid username");

        var expectedResponse = createFailApiResponse(body);

        // Act
        var result = mockMvc.perform(
                post("/users")
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
    public void createUser_emptyPassword_returnsError() throws Exception {
        // Arrange
        var requestDto = new CreateUserRequest("full name", "username", "");

        var body = new HashMap<String, String>();
        body.put("password", "size must be between 6 and 500");

        var expectedResponse = createFailApiResponse(body);

        // Act
        var result = mockMvc.perform(
                post("/users")
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
    public void createUser_duplicateUsername_returnsError() throws Exception {
        // Arrange
        var requestDto = getValidInputForCreateUser();

        var entity = new User(requestDto.getFullName(), requestDto.getUsername(), requestDto.getPassword());
        when(userService.createUser(entity))
                .thenThrow(new InvalidInputException("Username already exists"));

        var expectedResponse = createFailApiResponse("Username already exists");

        // Act
        var result = mockMvc.perform(
                post("/users")
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
                        fileUtils.createFileUrl(avatar.getName())
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
    public void updateUser_invalidUserId_returnsError() throws Exception {
        // Arrange
        var userId = 13;

        var exception = new EntityNotFoundException(User.class, userId);

        when(userService.getUser(userId))
                .thenThrow(exception);

        var requestDto = new UpdateUserRequest("full", "", null);

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
                        fileUtils.createFileUrl(file.getName())
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
}
