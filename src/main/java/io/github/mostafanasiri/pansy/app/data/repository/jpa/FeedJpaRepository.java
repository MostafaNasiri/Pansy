package io.github.mostafanasiri.pansy.app.data.repository.jpa;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.FeedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedJpaRepository extends JpaRepository<FeedEntity, Integer> {
    @Query("SELECT f.id FROM FeedEntity f")
    List<FeedEntity> getUserFeeds(List<Integer> userIds);
}