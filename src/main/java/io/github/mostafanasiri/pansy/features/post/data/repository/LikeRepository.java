package io.github.mostafanasiri.pansy.features.post.data.repository;

import io.github.mostafanasiri.pansy.features.post.data.entity.LikeEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, Integer> {
    boolean existsByPostIdAndUserId(int postId, int userId);

    Optional<LikeEntity> findByUserIdAndPostId(int userId, int postId);

    @Query("SELECT l.post.id FROM LikeEntity l WHERE l.post.id IN(?2) AND l.user.id=?1")
    List<Integer> getLikedPostIds(int user, List<Integer> postIds);

    @Query("""
            SELECT l, u
            FROM LikeEntity l
            INNER JOIN UserEntity u
            ON l.user.id=u.id
            WHERE l.post=?1
            ORDER BY l.createdAt DESC
            """)
    List<LikeEntity> getLikes(PostEntity post, Pageable pageable);
}
