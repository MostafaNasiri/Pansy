package io.github.mostafanasiri.pansy;

import io.github.mostafanasiri.pansy.auth.AuthController;
import io.github.mostafanasiri.pansy.auth.JwtTokenUtil;
import io.github.mostafanasiri.pansy.auth.dto.LoginRequest;
import io.github.mostafanasiri.pansy.auth.dto.RegisterRequest;
import io.github.mostafanasiri.pansy.common.exception.AuthenticationException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthController controller;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    public void register_successful_returnsToken() throws Exception {
        // Arrange
        var requestDto = getValidInputForRegister();

        var entity = new UserEntity(requestDto.getFullName(), requestDto.getUsername(), requestDto.getPassword());

        when(jwtTokenUtil.generateAccessToken(any()))
                .thenReturn("");
        when(userService.createUser(entity))
                .thenReturn(entity);

        // Act
        var result = mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isCreated());

        assertThat(response).contains("token");
    }

    private RegisterRequest getValidInputForRegister() {
        return new RegisterRequest("first last", "username", "pass123");
    }

    @Test
    public void register_emptyFullName_returnsError() throws Exception {
        // Arrange
        var requestDto = new RegisterRequest("", "username", "password");

        var body = new HashMap<String, String>();
        body.put("fullName", "size must be between 1 and 255");

        var expectedResponse = createFailApiResponse(body);

        // Act
        var result = mockMvc.perform(
                post("/auth/register")
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
    public void register_emptyUsername_returnsError() throws Exception {
        // Arrange
        var requestDto = new RegisterRequest("full name", "", "password");

        var body = new HashMap<String, String>();
        body.put("username", "Invalid username");

        var expectedResponse = createFailApiResponse(body);

        // Act
        var result = mockMvc.perform(
                post("/auth/register")
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
    public void register_emptyPassword_returnsError() throws Exception {
        // Arrange
        var requestDto = new RegisterRequest("full name", "username", "");

        var body = new HashMap<String, String>();
        body.put("password", "size must be between 6 and 500");

        var expectedResponse = createFailApiResponse(body);

        // Act
        var result = mockMvc.perform(
                post("/auth/register")
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
    public void register_duplicateUsername_returnsError() throws Exception {
        // Arrange
        var requestDto = getValidInputForRegister();

        var entity = new UserEntity(requestDto.getFullName(), requestDto.getUsername(), requestDto.getPassword());
        when(userService.createUser(entity))
                .thenThrow(new InvalidInputException("Username already exists"));

        var expectedResponse = createFailApiResponse("Username already exists");

        // Act
        var result = mockMvc.perform(
                post("/auth/register")
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
    public void login_invalidUsernameOrPassword_throwsAuthenticationException() throws Exception {
        // Arrange
        var requestDto = new LoginRequest("user", "password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(BadCredentialsException.class);

        // Act
        var result = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        // Assert
        result.andExpect(r -> assertTrue(null, r.getResolvedException() instanceof AuthenticationException));
    }

    @Test
    public void login_invalidUsernameOrPassword_returnsError() throws Exception {
        // Arrange
        var requestDto = new LoginRequest("user", "password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(BadCredentialsException.class);

        var expectedResponse = createFailApiResponse("Invalid username or password");

        // Act
        var result = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestDto))
        );

        var response = result.andReturn().getResponse().getContentAsString();

        // Assert
        result.andExpect(status().isUnauthorized());

        assertThat(response)
                .isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    public void login_emptyUsername_returnsError() throws Exception {
        // Arrange
        var requestDto = new LoginRequest("", "password");

        var body = new HashMap<String, String>();
        body.put("username", "Invalid username");

        var expectedResponse = createFailApiResponse(body);

        // Act
        var result = mockMvc.perform(
                post("/auth/login")
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
    public void login_emptyPassword_returnsError() throws Exception {
        // Arrange
        var requestDto = new LoginRequest("username", "");

        var body = new HashMap<String, String>();
        body.put("password", "size must be between 6 and 500");

        var expectedResponse = createFailApiResponse(body);

        // Act
        var result = mockMvc.perform(
                post("/auth/login")
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
