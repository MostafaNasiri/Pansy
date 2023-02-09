package io.github.mostafanasiri.pansy.features.feed.data;

import io.github.mostafanasiri.pansy.features.feed.data.entity.FeedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedJpaRepository extends JpaRepository<FeedEntity, Integer> {
    @Query("SELECT f.id FROM FeedEntity f")
    List<FeedEntity> getUserFeeds(List<Integer> userIds);
}