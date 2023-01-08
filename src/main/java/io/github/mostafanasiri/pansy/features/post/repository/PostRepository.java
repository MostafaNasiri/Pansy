package io.github.mostafanasiri.pansy.features.post.repository;

import io.github.mostafanasiri.pansy.features.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {
}
