package io.github.mostafanasiri.pansy.features.post.data.repository;

import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    @Query("""
            SELECT c, u
            FROM CommentEntity c
            INNER JOIN UserEntity u
            ON c.user.id=u.id
            WHERE c.post=?1
            ORDER BY c.createdAt DESC
            """)
    List<CommentEntity> getComments(PostEntity post, Pageable pageable);
}
