package io.github.mostafanasiri.pansy.features.post.data.repository;

import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Integer> {
    @Query(
            value = "SELECT file_id FROM post_images WHERE file_id IN(?1)",
            nativeQuery = true
    )
    List<Integer> getFileIdsThatAreAttachedToAPost(List<Integer> fileIds);

    List<PostEntity> findByUserOrderByCreatedAtDesc(UserEntity author, Pageable pageable);
}
