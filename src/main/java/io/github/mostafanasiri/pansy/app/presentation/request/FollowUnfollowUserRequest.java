package io.github.mostafanasiri.pansy.app.presentation.request;

import jakarta.validation.constraints.NotNull;

public record FollowUnfollowUserRequest(@NotNull int targetUserId) {
}
