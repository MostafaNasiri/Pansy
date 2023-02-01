package io.github.mostafanasiri.pansy.features.post.data.repository;

import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Integer> {
    @Query("""
            SELECT p
            FROM PostEntity p
            INNER JOIN FETCH p.images
            WHERE p.user=?1
            ORDER BY p.createdAt DESC
            """)
    List<PostEntity> getUserPosts(UserEntity user, Pageable pageable);
}
