package io.github.mostafanasiri.pansy.features.feed.domain;

import io.github.mostafanasiri.pansy.features.feed.data.FeedJpaRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedService {
    @Autowired
    private FeedJpaRepository feedJpaRepository;

    public void addPostToFollowersFeeds(Post post) {
        // Get post author's followers
        // Add post to author's followers' feeds
    }

    // TODO: removePostFromFollowersFeed(post)

    // TODO: removePostsFromFeed(feedOwner, targetUser)
}
