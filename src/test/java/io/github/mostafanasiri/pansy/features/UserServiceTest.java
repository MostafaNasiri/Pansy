package io.github.mostafanasiri.pansy.features;

import io.github.mostafanasiri.pansy.app.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.app.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.FollowerEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private FollowerJpaRepository followerJpaRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void createUser_duplicateUsername_throwsException() {
        // Arrange
        var username = "username";

        var user = new UserEntity("f", username, "");

        when(userJpaRepository.findByUsername(username))
                .thenReturn(new UserEntity());

        // Act & Assert
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> userService.createUser(user),
                ""
        );

        assertEquals(ex.getMessage(), "Username already exists");
    }

    @Test
    public void createUser_validInput_returnsCreatedUser() {
        // Arrange
        var user = new UserEntity("name", "username", "pass");

        when(userJpaRepository.save(user))
                .thenReturn(user);

        // Act
        var result = userService.createUser(user);

        // Assert
        assertEquals(user, result);
    }

    @Test
    public void updateUser_validInput_returnsUpdatedUser() {
        // Arrange
        var user = new UserEntity("name", "username", "pass");

        when(userJpaRepository.save(user))
                .thenReturn(user);

        // Act
        var result = userService.updateUser(user);

        // Assert
        assertEquals(result, user);
    }

    @Test
    public void getUser_invalidUserId_throwsException() {
        // Arrange
        var userId = 13;

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUser(userId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(UserEntity.class, userId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void getUser_validInput_returnsUser() {
        // Arrange
        var userId = 13;
        var user = new UserEntity();

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act
        var result = userService.getUser(userId);

        // Assert
        assertEquals(result, user);
    }

    @Test
    public void getFollowers_invalidUserId_throwsException() {
        // Arrange
        var userId = 13;

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getFollowers(userId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(UserEntity.class, userId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void getFollowers_validInput_returnsFollowers() {
        // Arrange
        var userId = 13;

        var user = new UserEntity();

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(user));

        var followers = new ArrayList<FollowerEntity>();
        followers.add(new FollowerEntity(new UserEntity("follower1", "", ""), user));

        when(followerJpaRepository.getFollowers(user))
                .thenReturn(followers);

        // Act
        var result = userService.getFollowers(userId);

        // Assert
        var expectedResult = followers.stream().map((f) -> f.getSourceUser()).toList();

        assertEquals(result, expectedResult);
    }

    @Test
    public void getFollowing_invalidUserId_throwsException() {
        // Arrange
        var userId = 13;

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getFollowing(userId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(UserEntity.class, userId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void getFollowing_validInput_returnsFollowing() {
        // Arrange
        var userId = 13;

        var user = new UserEntity();

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(user));

        var followers = new ArrayList<FollowerEntity>();
        followers.add(new FollowerEntity(new UserEntity("follower1", "", ""), user));

        when(followerJpaRepository.getFollowing(user))
                .thenReturn(followers);

        // Act
        var result = userService.getFollowing(userId);

        // Assert
        var expectedResult = followers.stream().map((f) -> f.getTargetUser()).toList();

        assertEquals(result, expectedResult);
    }

    @Test
    public void followUser_sourceUserIdSameAsTargetUserId_throwsException() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = sourceUserId;

        // Act & Assert
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> userService.followUser(sourceUserId, targetUserId),
                ""
        );

        assertEquals(ex.getMessage(), "A user can't follow him/herself!");
    }

    @Test
    public void followUser_invalidSourceUserId_throwsException() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        when(userJpaRepository.findById(sourceUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.followUser(sourceUserId, targetUserId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(UserEntity.class, sourceUserId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void followUser_invalidTargetUserId_throwsException() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        when(userJpaRepository.findById(sourceUserId))
                .thenReturn(Optional.of(new UserEntity()));
        when(userJpaRepository.findById(targetUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.followUser(sourceUserId, targetUserId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(UserEntity.class, targetUserId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void followUser_targetUserAlreadyFollowed_doesNothing() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        var sourceUser = new UserEntity();
        sourceUser.setId(sourceUserId);

        var targetUser = new UserEntity();
        targetUser.setId(targetUserId);

        when(userJpaRepository.findById(sourceUserId))
                .thenReturn(Optional.of(sourceUser));
        when(userJpaRepository.findById(targetUserId))
                .thenReturn(Optional.of(targetUser));

        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
                .thenReturn(new FollowerEntity());

        // Act
        userService.followUser(sourceUserId, targetUserId);

        // Assert
        verify(followerJpaRepository, never())
                .save(any());
    }

    @Test
    public void followUser_validInput_savesData() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        var sourceUser = new UserEntity();
        sourceUser.setId(sourceUserId);

        var targetUser = new UserEntity();
        targetUser.setId(targetUserId);

        when(userJpaRepository.findById(sourceUserId))
                .thenReturn(Optional.of(sourceUser));
        when(userJpaRepository.findById(targetUserId))
                .thenReturn(Optional.of(targetUser));

        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
                .thenReturn(null);

        // Act
        userService.followUser(sourceUserId, targetUserId);

        // Assert
        var follower = new FollowerEntity(sourceUser, targetUser);
        verify(followerJpaRepository, times(1))
                .save(follower);
    }

    @Test
    public void unfollowUser_sourceUserIdSameAsTargetUserId_throwsException() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = sourceUserId;

        // Act & Assert
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> userService.unfollowUser(sourceUserId, targetUserId),
                ""
        );

        assertEquals(ex.getMessage(), "A user can't unfollow him/herself!");
    }

    @Test
    public void unfollowUser_invalidSourceUserId_throwsException() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        when(userJpaRepository.findById(sourceUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.unfollowUser(sourceUserId, targetUserId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(UserEntity.class, sourceUserId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void unfollowUser_invalidTargetUserId_throwsException() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        when(userJpaRepository.findById(sourceUserId))
                .thenReturn(Optional.of(new UserEntity()));
        when(userJpaRepository.findById(targetUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.unfollowUser(sourceUserId, targetUserId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(UserEntity.class, targetUserId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void unfollowUser_targetUserNotFollowed_doesNothing() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        var sourceUser = new UserEntity();
        sourceUser.setId(sourceUserId);

        var targetUser = new UserEntity();
        targetUser.setId(targetUserId);

        when(userJpaRepository.findById(sourceUserId))
                .thenReturn(Optional.of(sourceUser));
        when(userJpaRepository.findById(targetUserId))
                .thenReturn(Optional.of(targetUser));

        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
                .thenReturn(null);

        // Act
        userService.unfollowUser(sourceUserId, targetUserId);

        // Assert
        verify(followerJpaRepository, never())
                .delete(any());
    }

    @Test
    public void unfollowUser_validInput_deletesData() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        var sourceUser = new UserEntity();
        sourceUser.setId(sourceUserId);

        var targetUser = new UserEntity();
        targetUser.setId(targetUserId);

        when(userJpaRepository.findById(sourceUserId))
                .thenReturn(Optional.of(sourceUser));
        when(userJpaRepository.findById(targetUserId))
                .thenReturn(Optional.of(targetUser));

        var follower = new FollowerEntity(sourceUser, targetUser);

        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
                .thenReturn(follower);

        // Act
        userService.unfollowUser(sourceUserId, targetUserId);

        // Assert
        verify(followerJpaRepository, times(1))
                .delete(follower);
    }
}
