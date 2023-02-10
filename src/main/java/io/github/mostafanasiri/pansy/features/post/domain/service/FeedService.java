package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.FeedEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.FeedJpaRepository;
import io.github.mostafanasiri.pansy.features.post.domain.PostDomainMapper;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.features.user.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

@Service
public class FeedService extends BaseService {
    private final static int MAX_FEED_SIZE = 10_000;

    @Autowired
    private UserService userService;
    @Autowired
    private FeedJpaRepository feedJpaRepository;
    @Autowired
    private FollowerJpaRepository followerJpaRepository;
    @Autowired
    private PostDomainMapper postDomainMapper;

    @Async
    public void addPostToFollowersFeeds(Post post) {
        // Get post author's followers
        var author = userService.getUser(post.user().id());
        var authorFollowerIds = followerJpaRepository.getFollowersIds(author.id());

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
    public void removePostFromFollowersFeeds(Post post) {
        // Get post author's followers
        var author = userService.getUser(post.user().id());
        var authorFollowerIds = followerJpaRepository.getFollowersIds(author.id());

        // Remove post from author's followers' feeds
        if (!authorFollowerIds.isEmpty()) {
            var followersFeeds = feedJpaRepository.findAllById(authorFollowerIds);

            followersFeeds.forEach(feed ->
                    feed.getItems()
                            .removeIf(item -> item.postId() == post.id())
            );

            feedJpaRepository.saveAll(followersFeeds);
        }
    }

    @Async
    public void removeAllPostsFromFeed(int feedOwnerUserId, int postsAuthorUserId) {
        var feedOwner = userService.getUser(feedOwnerUserId);
        var postsAuthor = userService.getUser(postsAuthorUserId);

        var feed = feedJpaRepository.findById(feedOwner.id());

        feed.ifPresent(f -> {
            f.getItems().removeIf(item -> item.userId() == postsAuthor.id());
            feedJpaRepository.save(f);
        });
    }
}
