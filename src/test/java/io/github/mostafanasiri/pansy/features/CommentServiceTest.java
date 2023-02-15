package io.github.mostafanasiri.pansy.features;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.mapper.CommentDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.Post;
import io.github.mostafanasiri.pansy.app.domain.service.CommentService;
import io.github.mostafanasiri.pansy.app.domain.service.NotificationService;
import io.github.mostafanasiri.pansy.app.domain.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    private final Post samplePost = new Post(0, "", new ArrayList<>());

    @Mock
    private PostService postService;
    @Mock
    private NotificationService notificationService;

    @Mock
    private CommentJpaRepository commentJpaRepository;
    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private PostJpaRepository postJpaRepository;

    @Mock
    private CommentDomainMapper commentDomainMapper;

    @InjectMocks
    private CommentService service;

    @Test
    public void getComments_validInput_inputsCorrectlyUsed() {
        // Arrange
        var postId = 1;
        var page = 0;
        var size = 1;
        var pageable = PageRequest.of(page, size);

        when(postService.getPost(anyInt()))
                .thenReturn(new Post(postId, "", new ArrayList<>()));

        // Act
        service.getComments(postId, page, size);

        // Assert
        Mockito.verify(postService)
                .getPost(postId);
        Mockito.verify(commentJpaRepository)
                .getComments(postId, pageable);
    }

    @Test
    public void getComments_validInput_returnsComments() {
        // Arrange
        List<CommentEntity> commentEntities = new ArrayList<>();
        commentEntities.add(new CommentEntity());

        when(postService.getPost(anyInt()))
                .thenReturn(samplePost);
        when(commentJpaRepository.getComments(anyInt(), any(Pageable.class)))
                .thenReturn(commentEntities);

        // Act
        var result = service.getComments(0, 0, 1);

        // Assert
        assertEquals(result.size(), commentEntities.size());
    }
}
