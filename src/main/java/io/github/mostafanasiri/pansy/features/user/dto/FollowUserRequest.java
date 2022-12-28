package io.github.mostafanasiri.pansy.features.user.dto;

import jakarta.validation.constraints.NotNull;

public record FollowUserRequest(@NotNull int userId) {
}
