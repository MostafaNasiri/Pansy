package io.github.mostafanasiri.pansy.features.post.data.repository.jpa;

import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.LikeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeJpaRepository extends JpaRepository<LikeEntity, Integer> {
    Optional<LikeEntity> findByUserIdAndPostId(int userId, int postId);

    @Query("""
            SELECT l.post.id
            FROM LikeEntity l
            WHERE l.post.id IN(?2) AND l.user.id=?1
            """)
    List<Integer> getLikedPostIds(int user, List<Integer> postIds);

    @Query("""
            SELECT l.user.id
            FROM LikeEntity l
            WHERE l.post.id=?1
            """)
    List<Integer> getLikerUserIds(int postId, Pageable pageable);

    @Query("""
            SELECT COUNT(l.id)
            FROM LikeEntity l
            WHERE l.post.id=?1
            """)
    int getPostLikeCount(int postId);
}
