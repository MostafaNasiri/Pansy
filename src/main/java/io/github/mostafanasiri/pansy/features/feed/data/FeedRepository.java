package io.github.mostafanasiri.pansy.features.feed.data;

import io.github.mostafanasiri.pansy.features.feed.data.entity.FeedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<FeedEntity, Integer> {

}
