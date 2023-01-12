package io.github.mostafanasiri.pansy.features.post.data.repository;

import io.github.mostafanasiri.pansy.features.post.data.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Integer> {
    long countByPostId(int postId);

    Optional<LikeEntity> findByUserIdAndPostId(int userId, int postId);
}
