package io.github.mostafanasiri.pansy.features;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.user.UserService;
import io.github.mostafanasiri.pansy.features.user.entity.Follower;
import io.github.mostafanasiri.pansy.features.user.entity.User;
import io.github.mostafanasiri.pansy.features.user.repo.FollowerRepository;
import io.github.mostafanasiri.pansy.features.user.repo.UserRepository;
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
    private UserRepository userRepository;

    @Mock
    private FollowerRepository followerRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void createUser_duplicateUsername_throwsException() {
        // Arrange
        var username = "username";

        var user = new User("f", username, "");

        when(userRepository.findByUsername(username))
                .thenReturn(new User());

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
        var user = new User("name", "username", "pass");

        when(userRepository.save(user))
                .thenReturn(user);

        // Act
        var result = userService.createUser(user);

        // Assert
        assertEquals(user, result);
    }

    @Test
    public void updateUser_validInput_returnsUpdatedUser() {
        // Arrange
        var user = new User("name", "username", "pass");

        when(userRepository.save(user))
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

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUser(userId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(User.class, userId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void getUser_validInput_returnsUser() {
        // Arrange
        var userId = 13;
        var user = new User();

        when(userRepository.findById(userId))
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

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getFollowers(userId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(User.class, userId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void getFollowers_validInput_returnsFollowers() {
        // Arrange
        var userId = 13;

        var user = new User();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        var followers = new ArrayList<Follower>();
        followers.add(new Follower(new User("follower1", "", ""), user));

        when(followerRepository.findAllByTargetUser(user))
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

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getFollowing(userId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(User.class, userId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void getFollowing_validInput_returnsFollowing() {
        // Arrange
        var userId = 13;

        var user = new User();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        var followers = new ArrayList<Follower>();
        followers.add(new Follower(new User("follower1", "", ""), user));

        when(followerRepository.findAllBySourceUser(user))
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

        when(userRepository.findById(sourceUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.followUser(sourceUserId, targetUserId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(User.class, sourceUserId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void followUser_invalidTargetUserId_throwsException() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        when(userRepository.findById(sourceUserId))
                .thenReturn(Optional.of(new User()));
        when(userRepository.findById(targetUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.followUser(sourceUserId, targetUserId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(User.class, targetUserId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void followUser_targetUserAlreadyFollowed_doesNothing() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        var sourceUser = new User();
        sourceUser.setId(sourceUserId);

        var targetUser = new User();
        targetUser.setId(targetUserId);

        when(userRepository.findById(sourceUserId))
                .thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(targetUserId))
                .thenReturn(Optional.of(targetUser));

        when(followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
                .thenReturn(new Follower());

        // Act
        userService.followUser(sourceUserId, targetUserId);

        // Assert
        verify(followerRepository, never())
                .save(any());
    }

    @Test
    public void followUser_validInput_savesData() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        var sourceUser = new User();
        sourceUser.setId(sourceUserId);

        var targetUser = new User();
        targetUser.setId(targetUserId);

        when(userRepository.findById(sourceUserId))
                .thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(targetUserId))
                .thenReturn(Optional.of(targetUser));

        when(followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
                .thenReturn(null);

        // Act
        userService.followUser(sourceUserId, targetUserId);

        // Assert
        var follower = new Follower(sourceUser, targetUser);
        verify(followerRepository, times(1))
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

        when(userRepository.findById(sourceUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.unfollowUser(sourceUserId, targetUserId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(User.class, sourceUserId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void unfollowUser_invalidTargetUserId_throwsException() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        when(userRepository.findById(sourceUserId))
                .thenReturn(Optional.of(new User()));
        when(userRepository.findById(targetUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.unfollowUser(sourceUserId, targetUserId),
                ""
        );

        var expectedMessage = new EntityNotFoundException(User.class, targetUserId).getMessage();

        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void unfollowUser_targetUserNotFollowed_doesNothing() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        var sourceUser = new User();
        sourceUser.setId(sourceUserId);

        var targetUser = new User();
        targetUser.setId(targetUserId);

        when(userRepository.findById(sourceUserId))
                .thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(targetUserId))
                .thenReturn(Optional.of(targetUser));

        when(followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
                .thenReturn(null);

        // Act
        userService.unfollowUser(sourceUserId, targetUserId);

        // Assert
        verify(followerRepository, never())
                .delete(any());
    }

    @Test
    public void unfollowUser_validInput_deletesData() {
        // Arrange
        var sourceUserId = 1;
        var targetUserId = 2;

        var sourceUser = new User();
        sourceUser.setId(sourceUserId);

        var targetUser = new User();
        targetUser.setId(targetUserId);

        when(userRepository.findById(sourceUserId))
                .thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(targetUserId))
                .thenReturn(Optional.of(targetUser));

        var follower = new Follower(sourceUser, targetUser);

        when(followerRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
                .thenReturn(follower);

        // Act
        userService.unfollowUser(sourceUserId, targetUserId);

        // Assert
        verify(followerRepository, times(1))
                .delete(follower);
    }
}
