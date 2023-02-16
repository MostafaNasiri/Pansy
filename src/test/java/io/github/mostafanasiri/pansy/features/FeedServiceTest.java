package io.github.mostafanasiri.pansy.features;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.FeedEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FeedJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.mapper.PostDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.Post;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.service.FeedService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class FeedServiceTest extends BaseServiceTest {
    @Mock
    private FeedJpaRepository feedJpaRepository;
    @Mock
    private FollowerJpaRepository followerJpaRepository;

    @Mock
    private PostDomainMapper postDomainMapper;

    @InjectMocks
    private FeedService service;

    @Test
    public void addPostToFollowersFeeds_successful_addsPost() {
        // Arrange
        var post = new Post(0, new User(1), null, null, 0, 0, null);

        List<Integer> followerIds = new ArrayList<>();
        followerIds.add(1);

        when(followerJpaRepository.getFollowersIds(post.getUser().id()))
                .thenReturn(followerIds);

        List<FeedEntity> feeds = new ArrayList<>();
        feeds.add(new FeedEntity());

        when(feedJpaRepository.findAllById(followerIds))
                .thenReturn(feeds);

        when(postDomainMapper.postToFeedItem(post))
                .thenReturn(new FeedEntity.FeedItem(post.getUser().id(), post.getId()));

        // Act
        service.addPostToFollowersFeeds(post);

        // Assert
        assertEquals(1, feeds.get(0).getItems().size());
    }

    @Test
    public void addPostToFollowersFeeds_successful_updatesDatabase() {
        // Arrange
        var post = new Post(0, new User(1), null, null, 0, 0, null);

        List<Integer> followerIds = new ArrayList<>();
        followerIds.add(1);

        when(followerJpaRepository.getFollowersIds(post.getUser().id()))
                .thenReturn(followerIds);

        List<FeedEntity> feeds = new ArrayList<>();
        feeds.add(new FeedEntity());

        when(feedJpaRepository.findAllById(followerIds))
                .thenReturn(feeds);

        when(postDomainMapper.postToFeedItem(post))
                .thenReturn(new FeedEntity.FeedItem(post.getUser().id(), post.getId()));

        // Act
        service.addPostToFollowersFeeds(post);

        // Assert
        verify(feedJpaRepository)
                .saveAll(feeds);
    }

    @Test
    public void removePostFromFollowersFeeds_successful_removesPost() {
        // Arrange
        var post = new Post(0, new User(1), null, null, 0, 0, null);

        List<Integer> followerIds = new ArrayList<>();
        followerIds.add(1);

        when(followerJpaRepository.getFollowersIds(post.getUser().id()))
                .thenReturn(followerIds);

        List<FeedEntity> feeds = new ArrayList<>();

        var feed = new FeedEntity();
        feed.getItems().add(new FeedEntity.FeedItem(post.getUser().id(), post.getId()));

        feeds.add(feed);

        when(feedJpaRepository.findAllById(followerIds))
                .thenReturn(feeds);

        // Act
        service.removePostFromFollowersFeeds(post);

        // Assert
        assertEquals(0, feeds.get(0).getItems().size());
    }

    @Test
    public void removePostFromFollowersFeeds_successful_updatesDatabase() {
        // Arrange
        var post = new Post(0, new User(1), null, null, 0, 0, null);

        List<Integer> followerIds = new ArrayList<>();
        followerIds.add(1);

        when(followerJpaRepository.getFollowersIds(post.getUser().id()))
                .thenReturn(followerIds);

        List<FeedEntity> feeds = new ArrayList<>();
        feeds.add(new FeedEntity());

        when(feedJpaRepository.findAllById(followerIds))
                .thenReturn(feeds);

        // Act
        service.removePostFromFollowersFeeds(post);

        // Assert
        verify(feedJpaRepository)
                .saveAll(feeds);
    }

    @Test
    public void removeAllPostsFromFeed_successful_removesPosts() {
        // Arrange
        var feedOwnerUserId = 1;
        var postsAuthorUserId = 2;

        var feed = new FeedEntity();
        feed.getItems().add(new FeedEntity.FeedItem(postsAuthorUserId, 1));
        feed.getItems().add(new FeedEntity.FeedItem(234, 1)); // We don't want this item to be removed

        when(feedJpaRepository.findById(feedOwnerUserId))
                .thenReturn(Optional.of(feed));

        // Act
        service.removeAllPostsFromFeed(feedOwnerUserId, postsAuthorUserId);

        // Assert
        assertEquals(1, feed.getItems().size());
    }

    @Test
    public void removeAllPostsFromFeed_successful_updatesDatabase() {
        // Arrange
        var feedOwnerUserId = 1;
        var postsAuthorUserId = 2;

        var feed = new FeedEntity();
        when(feedJpaRepository.findById(feedOwnerUserId))
                .thenReturn(Optional.of(feed));

        // Act
        service.removeAllPostsFromFeed(feedOwnerUserId, postsAuthorUserId);

        // Assert
        verify(feedJpaRepository)
                .save(feed);
    }
}
