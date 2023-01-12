package io.github.mostafanasiri.pansy.features.post.domain.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public record Post(
        @Nullable Integer id,
        @NonNull Author author,
        @NonNull String caption,
        @NonNull List<Image> images,
        int likesCount
) {
    public Post(@NonNull Author author, @NonNull String caption, @NonNull List<Image> images) {
        this(null, author, caption, images, 0);
    }
}
