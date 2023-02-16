package io.github.mostafanasiri.pansy.features;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.mapper.CommentDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.Comment;
import io.github.mostafanasiri.pansy.app.domain.model.Post;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.model.notification.CommentNotification;
import io.github.mostafanasiri.pansy.app.domain.service.CommentService;
import io.github.mostafanasiri.pansy.app.domain.service.NotificationService;
import io.github.mostafanasiri.pansy.app.domain.service.PostService;
import io.github.mostafanasiri.pansy.auth.AppUserDetails;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
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

    @BeforeAll
    public static void setup() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        AppUserDetails appUserDetails = Mockito.mock(AppUserDetails.class);
        when(appUserDetails.getUser())
                .thenReturn(Mockito.mock(UserEntity.class));
        when(appUserDetails.getUser().getId())
                .thenReturn(1);

        when(authentication.getPrincipal())
                .thenReturn(appUserDetails);
    }

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
    public void getComments_successful_returnsComments() {
        // Arrange
        List<CommentEntity> commentEntities = new ArrayList<>();
        commentEntities.add(new CommentEntity());

        when(postService.getPost(anyInt()))
                .thenReturn(new Post(0, null, null));
        when(commentJpaRepository.getComments(anyInt(), any(Pageable.class)))
                .thenReturn(commentEntities);

        // Act
        var result = service.getComments(0, 0, 1);

        // Assert
        assertEquals(result.size(), commentEntities.size());
    }

    @Test
    public void addComment_successful_savesCommentInDatabase() {
        // Arrange
        var postId = 1;
        var comment = new Comment("x");

        var commentator = new UserEntity();
        when(userJpaRepository.getReferenceById(anyInt()))
                .thenReturn(commentator);

        var post = new Post(postId, new User(1), null, null, null, null, null);
        when(postService.getPost(postId))
                .thenReturn(post);

        var commentEntity = new CommentEntity();
        when(commentJpaRepository.save(any()))
                .thenReturn(commentEntity);

        // Act
        service.addComment(postId, comment);

        // Assert
        Mockito.verify(commentJpaRepository)
                .save(any());
    }

    @Test
    public void addComment_successful_updatesPostCommentCount() {
        // Arrange
        var postId = 1;
        var comment = new Comment("x");

        var commentator = new UserEntity();
        when(userJpaRepository.getReferenceById(anyInt()))
                .thenReturn(commentator);

        var post = new Post(postId, new User(1), null, null, null, null, null);
        when(postService.getPost(postId))
                .thenReturn(post);

        var commentEntity = new CommentEntity();
        when(commentJpaRepository.save(any()))
                .thenReturn(commentEntity);

        var postCommentCount = 3;
        when(commentJpaRepository.getPostCommentCount(postId))
                .thenReturn(postCommentCount);

        // Act
        service.addComment(postId, comment);

        // Assert
        Mockito.verify(postService)
                .updatePostCommentCount(postId, postCommentCount);
    }

    @Test
    public void addComment_successful_addsCommentNotification() {
        // Arrange
        var postId = 1;
        var comment = new Comment("x");

        var commentator = new UserEntity();
        when(userJpaRepository.getReferenceById(anyInt()))
                .thenReturn(commentator);

        var post = new Post(postId, new User(1), null, null, null, null, null);
        when(postService.getPost(postId))
                .thenReturn(post);

        var commentEntity = new CommentEntity();
        when(commentJpaRepository.save(any()))
                .thenReturn(commentEntity);

        // Act
        service.addComment(postId, comment);

        // Assert
        var expectedNotification = new CommentNotification(
                new User(commentator.getId()),
                new User(post.getUser().id()),
                commentEntity.getId(),
                postId
        );
        Mockito.verify(notificationService)
                .addCommentNotification(expectedNotification);
    }
}
