package io.github.mostafanasiri.pansy.features.post.data.repository.jpa;

import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentJpaRepository extends JpaRepository<CommentEntity, Integer> {
    @Query("""
            SELECT c
            FROM CommentEntity c
            INNER JOIN FETCH c.user cu
            LEFT JOIN FETCH cu.avatar
            WHERE c.post=?1
            ORDER BY c.createdAt DESC
            """)
    List<CommentEntity> getComments(PostEntity post, Pageable pageable);
}
