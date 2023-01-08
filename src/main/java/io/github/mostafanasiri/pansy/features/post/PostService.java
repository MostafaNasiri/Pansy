package io.github.mostafanasiri.pansy.features.post;

import io.github.mostafanasiri.pansy.features.post.entity.Post;
import io.github.mostafanasiri.pansy.features.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public Post createPost(Post post) {
        return postRepository.save(post);
    }
}
