package io.github.mostafanasiri.pansy.features.post.data.repository;

import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
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
            INNER JOIN p.images
            WHERE p.id=?1
            """)
    List<PostEntity> getPostsByUser(int userId, Pageable pageable);
}
