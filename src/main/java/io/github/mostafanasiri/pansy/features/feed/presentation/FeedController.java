package io.github.mostafanasiri.pansy.features.feed.presentation;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.features.feed.domain.FeedService;
import io.github.mostafanasiri.pansy.features.post.presentation.response.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Feed")
@RestController
public class FeedController {
    @Autowired
    private FeedService service;

    @GetMapping("/users/{user_id}/feed")
    @Operation(summary = "Returns a list of users who are followers of the specified user id")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getFollowers(
            @PathVariable(name = "user_id") int userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "30") @Max(50) int size
    ) {

        return null;
    }
}
