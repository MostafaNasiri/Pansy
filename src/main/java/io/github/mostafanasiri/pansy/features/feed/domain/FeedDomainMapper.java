package io.github.mostafanasiri.pansy.features.feed.domain;

import io.github.mostafanasiri.pansy.features.feed.data.entity.FeedEntity;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import org.springframework.stereotype.Component;

@Component
public class FeedDomainMapper {
    public FeedEntity.FeedItem postToFeedItem(Post post) {
        return new FeedEntity.FeedItem(
                post.user().id(),
                post.id(),
                post.createdAt().getTime()
        );
    }
}
