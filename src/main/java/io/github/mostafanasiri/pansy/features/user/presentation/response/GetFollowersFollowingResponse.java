package io.github.mostafanasiri.pansy.features.user.presentation.response;

import java.util.List;

public record GetFollowersFollowingResponse(List<Item> items) {
    public record Item(int id, String fullName, String username, String avatarUrl) {
    }
}
