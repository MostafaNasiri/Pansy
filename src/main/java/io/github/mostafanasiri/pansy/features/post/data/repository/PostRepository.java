package io.github.mostafanasiri.pansy.features.post.data.repository;

import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Integer> {

    /**
     * @param authenticatedUser This parameter is needed to check if each post is liked by the authenticated user.
     */
    @Query("""
                SELECT p, l
                FROM PostEntity p
                INNER JOIN p.images
                LEFT JOIN p.likes l
                WHERE p.user=?1
                ORDER BY p.createdAt DESC
            """)
    List<PostEntity> getUserPosts(UserEntity user, UserEntity authenticatedUser, Pageable pageable);
}
