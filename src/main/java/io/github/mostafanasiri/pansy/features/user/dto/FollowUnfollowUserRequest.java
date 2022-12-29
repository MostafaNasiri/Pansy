package io.github.mostafanasiri.pansy.features.user.dto;

import jakarta.validation.constraints.NotNull;

public record FollowUnfollowUserRequest(@NotNull int targetUserId) {
}
