package io.github.mostafanasiri.pansy.app.domain.service;

import io.github.mostafanasiri.pansy.app.common.BaseService;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.FeedEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FeedJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.mapper.PostDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

@Service
public class FeedService extends BaseService {
    private final static int MAX_FEED_SIZE = 10_000;

    @Autowired
    private FeedJpaRepository feedJpaRepository;
    @Autowired
    private FollowerJpaRepository followerJpaRepository;

    @Autowired
    private PostDomainMapper postDomainMapper;

    @Async
    public void addPostToFollowersFeeds(@NonNull Post post) {
        // Get post author's followers
        var authorFollowerIds = followerJpaRepository.getFollowersIds(post.getUser().id());

        // Add post to author's followers' feeds
        if (!authorFollowerIds.isEmpty()) {
            var followersFeeds = feedJpaRepository.findAllById(authorFollowerIds);
            followersFeeds.forEach(feed -> addPostToFeed(post, feed));

            feedJpaRepository.saveAll(followersFeeds);
        }
    }

    private void addPostToFeed(Post post, FeedEntity feed) {
        var feedItems = feed.getItems();

        feedItems.addFirst(postDomainMapper.postToFeedItem(post));

        // Make sure that the feed doesn't grow larger than MAX_FEED_SIZE
        feedItems = feedItems.stream()
                .limit(MAX_FEED_SIZE)
                .collect(Collectors.toCollection(ArrayDeque::new));

        feed.setItems(feedItems);
    }

    @Async
    public void removePostFromFollowersFeeds(@NonNull Post post) {
        // Get post author's followers
        var authorFollowerIds = followerJpaRepository.getFollowersIds(post.getUser().id());

        // Remove post from author's followers' feeds
        if (!authorFollowerIds.isEmpty()) {
            var followersFeeds = feedJpaRepository.findAllById(authorFollowerIds);

            followersFeeds.forEach(feed ->
                    feed.getItems()
                            .removeIf(item -> item.postId() == post.getId())
            );

            feedJpaRepository.saveAll(followersFeeds);
        }
    }

    @Async
    public void removeAllPostsFromFeed(int feedOwnerUserId, int postsAuthorUserId) {
        var feed = feedJpaRepository.findById(feedOwnerUserId);

        feed.ifPresent(f -> {
            f.getItems().removeIf(item -> item.userId() == postsAuthorUserId);
            feedJpaRepository.save(f);
        });
    }
}
