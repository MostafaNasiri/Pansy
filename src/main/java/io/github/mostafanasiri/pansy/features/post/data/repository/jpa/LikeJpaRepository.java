package io.github.mostafanasiri.pansy.features.post.data.repository.jpa;

import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.LikeEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
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
            SELECT l
            FROM LikeEntity l
            INNER JOIN FETCH l.user lu
            LEFT JOIN FETCH lu.avatar
            WHERE l.post=?1
            ORDER BY l.createdAt DESC
            """)
    List<LikeEntity> getLikes(PostEntity post, Pageable pageable);

    @Query("""
            SELECT COUNT(l.id)
            FROM LikeEntity l
            WHERE l.post=?1
            """)
    int getPostLikeCount(PostEntity post);
}
