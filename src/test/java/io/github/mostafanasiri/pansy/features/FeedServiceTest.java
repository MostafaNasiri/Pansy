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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void addPostToFollowersFeeds_successful_addsPostToFeeds() {
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
        assertEquals(feeds.get(0).getItems().size(), 1);
    }
}
