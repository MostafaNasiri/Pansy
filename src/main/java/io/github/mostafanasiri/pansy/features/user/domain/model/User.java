package io.github.mostafanasiri.pansy.features.user.domain.model;

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
) {
    public User(String fullName, String username, String password) {
        this(null, fullName, username, password, null, null, null, null, null);
    }

    public User(Integer id, String fullName, Image avatar) {
        this(id, fullName, null, null, avatar, null, null, null, null);
    }
}
