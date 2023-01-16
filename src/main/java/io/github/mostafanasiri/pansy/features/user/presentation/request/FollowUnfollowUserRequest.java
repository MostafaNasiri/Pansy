package io.github.mostafanasiri.pansy.features.user.presentation.request;

import jakarta.validation.constraints.NotNull;

public record FollowUnfollowUserRequest(@NotNull int targetUserId) {
}
