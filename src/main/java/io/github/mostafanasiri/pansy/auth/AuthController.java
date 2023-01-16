package io.github.mostafanasiri.pansy.auth;

import io.github.mostafanasiri.pansy.auth.dto.LoginRequest;
import io.github.mostafanasiri.pansy.auth.dto.LoginResponse;
import io.github.mostafanasiri.pansy.auth.dto.RegisterRequest;
import io.github.mostafanasiri.pansy.auth.dto.RegisterResponse;
import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.common.exception.AuthenticationException;
import io.github.mostafanasiri.pansy.features.user.domain.UserService;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    @Operation(summary = "Creates a new user")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        var user = new User(request.getFullName(), request.getUsername(), request.getPassword());
        var createdUser = userService.createUser(user);

        var token = jwtTokenUtil.generateAccessToken(createdUser.username());
        var response = new RegisterResponse(createdUser.id(), token);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Returns an access token for the given username")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            var userDetails = (UserDetails) authentication.getPrincipal();

            var token = jwtTokenUtil.generateAccessToken(userDetails.getUsername());
            var response = new LoginResponse(token);

            return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, response), HttpStatus.OK);
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid username or password");
        }
    }
}
