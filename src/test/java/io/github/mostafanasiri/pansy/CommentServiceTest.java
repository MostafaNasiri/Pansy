package io.github.mostafanasiri.pansy;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.app.domain.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.app.domain.mapper.CommentDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.Comment;
import io.github.mostafanasiri.pansy.app.domain.model.Post;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.model.notification.CommentNotification;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest extends BaseServiceTest {
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
    public void getComments_validInput_usesInputsCorrectly() {
        // Arrange
        var postId = 1;
        var page = 0;
        var size = 1;
        var pageRequest = PageRequest.of(page, size);

        when(postService.getPost(anyInt()))
                .thenReturn(new Post(postId, "", new ArrayList<>()));

        // Act
        service.getComments(postId, page, size);

        // Assert
        verify(commentJpaRepository)
                .getComments(postId, pageRequest);
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
        assertEquals(commentEntities.size(), result.size());
    }

    @Test
    public void addComment_successful_storesCommentInDatabase() {
        // Arrange
        var postId = 1;
        var comment = new Comment("x");

        var commentator = new UserEntity();
        when(userJpaRepository.getReferenceById(anyInt()))
                .thenReturn(commentator);

        var post = new Post(postId, new User(1), null, null, 0, 0, null);
        when(postService.getPost(postId))
                .thenReturn(post);

        var commentEntity = new CommentEntity();
        when(commentJpaRepository.save(any()))
                .thenReturn(commentEntity);

        // Act
        service.addComment(postId, comment);

        // Assert
        verify(commentJpaRepository)
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

        var post = new Post(postId, new User(1), null, null, 0, 0, null);
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
        verify(postService)
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

        var post = new Post(postId, new User(1), null, null, 0, 0, null);
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
        verify(notificationService)
                .addCommentNotification(expectedNotification);
    }

    @Test
    public void deleteComment_commentNotBelongingToAuthenticatedUser_throwsException() {
        // Arrange
        var postId = 1;
        var commentId = 2;

        var commentEntity = Mockito.mock(CommentEntity.class);
        when(commentEntity.getUser())
                .thenReturn(Mockito.mock(UserEntity.class));
        when(commentEntity.getUser().getId())
                .thenReturn(AUTHENTICATED_USER_ID * 2);

        when(commentJpaRepository.findById(commentId))
                .thenReturn(Optional.of(commentEntity));

        // Act & Assert
        var exception = assertThrows(
                AuthorizationException.class,
                () -> service.deleteComment(postId, commentId),
                ""
        );
        var expectedMessage = "Comment does not belong to the authenticated user";

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void deleteComment_commentNotBelongingToPost_throwsException() {
        // Arrange
        var postId = 1;
        var commentId = 2;

        var commentEntity = Mockito.mock(CommentEntity.class);
        when(commentEntity.getUser())
                .thenReturn(Mockito.mock(UserEntity.class));
        when(commentEntity.getUser().getId())
                .thenReturn(AUTHENTICATED_USER_ID);
        when(commentEntity.getPost())
                .thenReturn(Mockito.mock(PostEntity.class));
        when(commentEntity.getPost().getId())
                .thenReturn(postId * 2);

        when(commentJpaRepository.findById(commentId))
                .thenReturn(Optional.of(commentEntity));

        when(postService.getPost(postId))
                .thenReturn(new Post(postId, null, null));

        // Act & Assert
        var exception = assertThrows(
                InvalidInputException.class,
                () -> service.deleteComment(postId, commentId),
                ""
        );
        var expectedMessage = "Comment does not belong to this post";

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void deleteComment_successful_deletesCommentFromDatabase() {
        // Arrange
        var postId = 1;
        var commentId = 2;

        var commentEntity = Mockito.mock(CommentEntity.class);
        when(commentEntity.getUser())
                .thenReturn(Mockito.mock(UserEntity.class));
        when(commentEntity.getUser().getId())
                .thenReturn(AUTHENTICATED_USER_ID);
        when(commentEntity.getPost())
                .thenReturn(Mockito.mock(PostEntity.class));
        when(commentEntity.getPost().getId())
                .thenReturn(postId);

        when(commentJpaRepository.findById(commentId))
                .thenReturn(Optional.of(commentEntity));

        when(postService.getPost(postId))
                .thenReturn(new Post(postId, null, null));

        // Act
        service.deleteComment(postId, commentId);

        // Assert
        verify(commentJpaRepository)
                .delete(commentEntity);
    }

    @Test
    public void deleteComment_successful_updatesPostCommentCount() {
        // Arrange
        var postId = 1;
        var commentId = 2;

        var commentEntity = Mockito.mock(CommentEntity.class);
        when(commentEntity.getUser())
                .thenReturn(Mockito.mock(UserEntity.class));
        when(commentEntity.getUser().getId())
                .thenReturn(AUTHENTICATED_USER_ID);
        when(commentEntity.getPost())
                .thenReturn(Mockito.mock(PostEntity.class));
        when(commentEntity.getPost().getId())
                .thenReturn(postId);

        when(commentJpaRepository.findById(commentId))
                .thenReturn(Optional.of(commentEntity));

        when(postService.getPost(postId))
                .thenReturn(new Post(postId, null, null));

        var postCommentCount = 3;
        when(commentJpaRepository.getPostCommentCount(postId))
                .thenReturn(postCommentCount);

        // Act
        service.deleteComment(postId, commentId);

        // Assert
        verify(postService)
                .updatePostCommentCount(postId, postCommentCount);
    }

    @Test
    public void deleteComment_successful_deletesCommentNotification() {
        // Arrange
        var postId = 1;
        var commentId = 2;

        var commentEntity = Mockito.mock(CommentEntity.class);
        when(commentEntity.getId())
                .thenReturn(commentId);
        when(commentEntity.getUser())
                .thenReturn(Mockito.mock(UserEntity.class));
        when(commentEntity.getUser().getId())
                .thenReturn(AUTHENTICATED_USER_ID);
        when(commentEntity.getPost())
                .thenReturn(Mockito.mock(PostEntity.class));
        when(commentEntity.getPost().getId())
                .thenReturn(postId);

        when(commentJpaRepository.findById(commentId))
                .thenReturn(Optional.of(commentEntity));

        when(postService.getPost(postId))
                .thenReturn(new Post(postId, null, null));

        // Act
        service.deleteComment(postId, commentId);

        // Assert
        verify(notificationService)
                .deleteCommentNotification(commentId);
    }
}
