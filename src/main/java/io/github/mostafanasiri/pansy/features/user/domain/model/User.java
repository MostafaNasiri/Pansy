package io.github.mostafanasiri.pansy.features.user.domain.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record User(
        @Nullable Integer id,
        @NonNull String fullName,
        @Nullable String username,
        @Nullable String password,
        @Nullable Image avatar,
        @Nullable String bio,
        @Nullable Integer postCount,
        @Nullable Integer followerCount,
        @Nullable Integer followingCount
) {
    public User(@NonNull String fullName, @NonNull String username, @NonNull String password) {
        this(null, fullName, username, password, null, null, null, null, null);
    }

    public User(@Nullable Integer id, @NonNull String fullName, @Nullable Image avatar) {
        this(id, fullName, null, null, avatar, null, null, null, null);
    }
}
