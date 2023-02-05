package io.github.mostafanasiri.pansy.features.user.domain.model;

import java.io.Serializable;

public record User(
        Integer id,
        String fullName,
        String username,
        String password,
        Image avatar,
        String bio,
        Integer postCount,
        Integer followerCount,
        Integer followingCount
) implements Serializable {
    public User(int id) {
        this(id, null, null, null, null, null, null, null, null);
    }

    public User(String fullName, String username, String password) {
        this(null, fullName, username, password, null, null, null, null, null);
    }

    public User(Integer id, String fullName, Image avatar, String bio) {
        this(id, fullName, null, null, avatar, bio, null, null, null);
    }
}
