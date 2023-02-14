package io.github.mostafanasiri.pansy.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mostafanasiri.pansy.app.common.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final static String BEARER = "Bearer ";

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!hasAuthHeader(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = getToken(request);

        String username;
        try {
            username = jwtTokenUtil.getUsernameFromToken(token);
        } catch (MalformedJwtException | UnsupportedJwtException e) {
            handleError(response, "Invalid token", HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (ExpiredJwtException e) {
            handleError(response, "The token is expired", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        setAuthenticationContext(username, request);

        filterChain.doFilter(request, response);
    }

    private boolean hasAuthHeader(HttpServletRequest request) {
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authHeader != null && authHeader.startsWith(BEARER);
    }

    private String getToken(HttpServletRequest request) {
        var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return header.substring(BEARER.length());
    }

    private void handleError(
            HttpServletResponse response,
            String message,
            int statusCode
    ) throws IOException {
        try {
            var errorResponse = new ObjectMapper().writeValueAsString(
                    new ApiResponse<>(ApiResponse.Status.FAIL, message)
            );

            response.setContentType("application/json");
            response.setStatus(statusCode);
            response.getWriter().write(errorResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void setAuthenticationContext(String username, HttpServletRequest request) {
        var userDetails = jwtUserDetailsService.loadUserByUsername(username);

        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
