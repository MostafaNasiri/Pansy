package io.github.mostafanasiri.pansy.features.user.dto;

import java.util.List;

public record GetFollowersFollowingResponse(List<Item> items) {
    public record Item(int id, String name, String avatarUrl) {
    }
}
