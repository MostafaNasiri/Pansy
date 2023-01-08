package io.github.mostafanasiri.pansy.features.post.dto;

import java.util.List;

public record CreatePostResponse(int id, String caption, List<String> images) {
}
