package io.github.mostafanasiri.pansy.features;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.app.data.entity.redis.UserRedis;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FeedJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FileJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.app.domain.mapper.UserDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.service.FeedService;
import io.github.mostafanasiri.pansy.app.domain.service.FileService;
import io.github.mostafanasiri.pansy.app.domain.service.NotificationService;
import io.github.mostafanasiri.pansy.app.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private UserRedisRepository userRedisRepository;
    @Mock
    private FollowerJpaRepository followerJpaRepository;
    @Mock
    private FeedJpaRepository feedJpaRepository;
    @Mock
    private FileJpaRepository fileJpaRepository;

    @Mock
    private NotificationService notificationService;
    @Mock
    private FeedService feedService;
    @Mock
    private FileService fileService;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserDomainMapper userDomainMapper;

    @InjectMocks
    private UserService service;

    @Test
    public void getUsers_successful_fetchesUnCachedUsersFromDatabase() {
        // Arrange
        var userIds = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

        var cachedUsersRedis = Arrays.asList(
                new UserRedis(userIds.get(0), null, null, null, null, 0, 0, 0),
                new UserRedis(userIds.get(1), null, null, null, null, 0, 0, 0)
        );
        when(userRedisRepository.findAllById(userIds))
                .thenReturn(cachedUsersRedis);

        var cachedUsers = Arrays.asList(
                new User(cachedUsersRedis.get(0).getId()),
                new User(cachedUsersRedis.get(1).getId())
        );
        when(userDomainMapper.usersRedisToUsers(cachedUsersRedis))
                .thenReturn(cachedUsers);

        // Act
        service.getUsers(userIds);

        // Assert
        var cachedUserIds = cachedUsers.stream().map(User::id).toList();
        userIds.removeAll(cachedUserIds);

        verify(userJpaRepository)
                .findAllById(userIds);
    }

    @Test
    public void getUsers_successful_savesUnCachedUsersInRedis() {
        // Arrange
        var userIds = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

        var unCachedUsers = List.of(new User(0));
        when(userDomainMapper.userEntitiesToUsers(any()))
                .thenReturn(unCachedUsers);

        // Act
        service.getUsers(userIds);

        // Assert
        verify(userRedisRepository)
                .saveAll(any());
    }

    @Test
    public void getUsers_noUnCachedUsers_doesNotHitDatabase() {
        // Arrange
        var userIds = new ArrayList<>(Arrays.asList(1, 2));

        var cachedUsersRedis = Arrays.asList(
                new UserRedis(userIds.get(0), null, null, null, null, 0, 0, 0),
                new UserRedis(userIds.get(1), null, null, null, null, 0, 0, 0)
        );
        when(userRedisRepository.findAllById(userIds))
                .thenReturn(cachedUsersRedis);

        var cachedUsers = Arrays.asList(
                new User(cachedUsersRedis.get(0).getId()),
                new User(cachedUsersRedis.get(1).getId())
        );
        when(userDomainMapper.usersRedisToUsers(cachedUsersRedis))
                .thenReturn(cachedUsers);

        // Act
        service.getUsers(userIds);

        // Assert
        verify(userJpaRepository, times(0))
                .findAllById(any());
    }

    @Test
    public void getUser_userExistsInCache_returnsCachedUser() {
        // Arrange
        var userId = 1;

        var userRedis = new UserRedis(userId, null, null, null, null, 0, 0, 0);
        when(userRedisRepository.findById(userId))
                .thenReturn(Optional.of(userRedis));

        var expectedResult = new User(userRedis.getId());
        when(userDomainMapper.userRedisToUser(userRedis))
                .thenReturn(expectedResult);

        // Act
        var result = service.getUser(userId);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    public void getUser_userExistsInCache_doesNotHitDatabase() {
        // Arrange
        var userId = 1;

        var userRedis = new UserRedis(userId, null, null, null, null, 0, 0, 0);
        when(userRedisRepository.findById(userId))
                .thenReturn(Optional.of(userRedis));

        // Act
        service.getUser(userId);

        // Assert
        verify(userJpaRepository, times(0))
                .findById(userId);
    }

    @Test
    public void getUser_userDoesNotExistInCache_fetchesUserFromDatabase() {
        // Arrange
        var userId = 1;

        when(userRedisRepository.findById(userId))
                .thenReturn(Optional.empty());

        var userEntity = new UserEntity();
        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        var expectedResult = new User(userId);
        when(userDomainMapper.userEntityToUser(userEntity))
                .thenReturn(expectedResult);

        // Act
        var result = service.getUser(userId);

        // Assert
        verify(userJpaRepository)
                .findById(userId);

        assertEquals(expectedResult, result);
    }

    @Test
    public void getUser_userDoesNotExistInCache_savesUserInRedis() {
        // Arrange
        var userId = 1;

        when(userRedisRepository.findById(userId))
                .thenReturn(Optional.empty());

        var userEntity = new UserEntity();
        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        var user = new User(userId);
        when(userDomainMapper.userEntityToUser(userEntity))
                .thenReturn(user);

        // Act
        service.getUser(userId);

        // Assert
        verify(userRedisRepository)
                .save(any());
    }

//    @Test
//    public void createUser_duplicateUsername_throwsException() {
//        // Arrange
//        var username = "username";
//
//        var user = new UserEntity("f", username, "");
//
//        when(userJpaRepository.findByUsername(username))
//                .thenReturn(new UserEntity());
//
//        // Act & Assert
//        InvalidInputException ex = assertThrows(
//                InvalidInputException.class,
//                () -> userService.createUser(user),
//                ""
//        );
//
//        assertEquals(ex.getMessage(), "Username already exists");
//    }
//
//    @Test
//    public void createUser_validInput_returnsCreatedUser() {
//        // Arrange
//        var user = new UserEntity("name", "username", "pass");
//
//        when(userJpaRepository.save(user))
//                .thenReturn(user);
//
//        // Act
//        var result = userService.createUser(user);
//
//        // Assert
//        assertEquals(user, result);
//    }
//
//    @Test
//    public void updateUser_validInput_returnsUpdatedUser() {
//        // Arrange
//        var user = new UserEntity("name", "username", "pass");
//
//        when(userJpaRepository.save(user))
//                .thenReturn(user);
//
//        // Act
//        var result = userService.updateUser(user);
//
//        // Assert
//        assertEquals(result, user);
//    }
//
//    @Test
//    public void getUser_invalidUserId_throwsException() {
//        // Arrange
//        var userId = 13;
//
//        when(userJpaRepository.findById(userId))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        EntityNotFoundException ex = assertThrows(
//                EntityNotFoundException.class,
//                () -> userService.getUser(userId),
//                ""
//        );
//
//        var expectedMessage = new EntityNotFoundException(UserEntity.class, userId).getMessage();
//
//        assertEquals(ex.getMessage(), expectedMessage);
//    }
//
//    @Test
//    public void getUser_validInput_returnsUser() {
//        // Arrange
//        var userId = 13;
//        var user = new UserEntity();
//
//        when(userJpaRepository.findById(userId))
//                .thenReturn(Optional.of(user));
//
//        // Act
//        var result = userService.getUser(userId);
//
//        // Assert
//        assertEquals(result, user);
//    }
//
//    @Test
//    public void getFollowers_invalidUserId_throwsException() {
//        // Arrange
//        var userId = 13;
//
//        when(userJpaRepository.findById(userId))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        EntityNotFoundException ex = assertThrows(
//                EntityNotFoundException.class,
//                () -> userService.getFollowers(userId),
//                ""
//        );
//
//        var expectedMessage = new EntityNotFoundException(UserEntity.class, userId).getMessage();
//
//        assertEquals(ex.getMessage(), expectedMessage);
//    }
//
//    @Test
//    public void getFollowers_validInput_returnsFollowers() {
//        // Arrange
//        var userId = 13;
//
//        var user = new UserEntity();
//
//        when(userJpaRepository.findById(userId))
//                .thenReturn(Optional.of(user));
//
//        var followers = new ArrayList<FollowerEntity>();
//        followers.add(new FollowerEntity(new UserEntity("follower1", "", ""), user));
//
//        when(followerJpaRepository.getFollowers(user))
//                .thenReturn(followers);
//
//        // Act
//        var result = userService.getFollowers(userId);
//
//        // Assert
//        var expectedResult = followers.stream().map((f) -> f.getSourceUser()).toList();
//
//        assertEquals(result, expectedResult);
//    }
//
//    @Test
//    public void getFollowing_invalidUserId_throwsException() {
//        // Arrange
//        var userId = 13;
//
//        when(userJpaRepository.findById(userId))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        EntityNotFoundException ex = assertThrows(
//                EntityNotFoundException.class,
//                () -> userService.getFollowing(userId),
//                ""
//        );
//
//        var expectedMessage = new EntityNotFoundException(UserEntity.class, userId).getMessage();
//
//        assertEquals(ex.getMessage(), expectedMessage);
//    }
//
//    @Test
//    public void getFollowing_validInput_returnsFollowing() {
//        // Arrange
//        var userId = 13;
//
//        var user = new UserEntity();
//
//        when(userJpaRepository.findById(userId))
//                .thenReturn(Optional.of(user));
//
//        var followers = new ArrayList<FollowerEntity>();
//        followers.add(new FollowerEntity(new UserEntity("follower1", "", ""), user));
//
//        when(followerJpaRepository.getFollowing(user))
//                .thenReturn(followers);
//
//        // Act
//        var result = userService.getFollowing(userId);
//
//        // Assert
//        var expectedResult = followers.stream().map((f) -> f.getTargetUser()).toList();
//
//        assertEquals(result, expectedResult);
//    }
//
//    @Test
//    public void followUser_sourceUserIdSameAsTargetUserId_throwsException() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = sourceUserId;
//
//        // Act & Assert
//        InvalidInputException ex = assertThrows(
//                InvalidInputException.class,
//                () -> userService.followUser(sourceUserId, targetUserId),
//                ""
//        );
//
//        assertEquals(ex.getMessage(), "A user can't follow him/herself!");
//    }
//
//    @Test
//    public void followUser_invalidSourceUserId_throwsException() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = 2;
//
//        when(userJpaRepository.findById(sourceUserId))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        EntityNotFoundException ex = assertThrows(
//                EntityNotFoundException.class,
//                () -> userService.followUser(sourceUserId, targetUserId),
//                ""
//        );
//
//        var expectedMessage = new EntityNotFoundException(UserEntity.class, sourceUserId).getMessage();
//
//        assertEquals(ex.getMessage(), expectedMessage);
//    }
//
//    @Test
//    public void followUser_invalidTargetUserId_throwsException() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = 2;
//
//        when(userJpaRepository.findById(sourceUserId))
//                .thenReturn(Optional.of(new UserEntity()));
//        when(userJpaRepository.findById(targetUserId))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        EntityNotFoundException ex = assertThrows(
//                EntityNotFoundException.class,
//                () -> userService.followUser(sourceUserId, targetUserId),
//                ""
//        );
//
//        var expectedMessage = new EntityNotFoundException(UserEntity.class, targetUserId).getMessage();
//
//        assertEquals(ex.getMessage(), expectedMessage);
//    }
//
//    @Test
//    public void followUser_targetUserAlreadyFollowed_doesNothing() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = 2;
//
//        var sourceUser = new UserEntity();
//        sourceUser.setId(sourceUserId);
//
//        var targetUser = new UserEntity();
//        targetUser.setId(targetUserId);
//
//        when(userJpaRepository.findById(sourceUserId))
//                .thenReturn(Optional.of(sourceUser));
//        when(userJpaRepository.findById(targetUserId))
//                .thenReturn(Optional.of(targetUser));
//
//        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
//                .thenReturn(new FollowerEntity());
//
//        // Act
//        userService.followUser(sourceUserId, targetUserId);
//
//        // Assert
//        verify(followerJpaRepository, never())
//                .save(any());
//    }
//
//    @Test
//    public void followUser_validInput_savesData() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = 2;
//
//        var sourceUser = new UserEntity();
//        sourceUser.setId(sourceUserId);
//
//        var targetUser = new UserEntity();
//        targetUser.setId(targetUserId);
//
//        when(userJpaRepository.findById(sourceUserId))
//                .thenReturn(Optional.of(sourceUser));
//        when(userJpaRepository.findById(targetUserId))
//                .thenReturn(Optional.of(targetUser));
//
//        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
//                .thenReturn(null);
//
//        // Act
//        userService.followUser(sourceUserId, targetUserId);
//
//        // Assert
//        var follower = new FollowerEntity(sourceUser, targetUser);
//        verify(followerJpaRepository, times(1))
//                .save(follower);
//    }
//
//    @Test
//    public void unfollowUser_sourceUserIdSameAsTargetUserId_throwsException() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = sourceUserId;
//
//        // Act & Assert
//        InvalidInputException ex = assertThrows(
//                InvalidInputException.class,
//                () -> userService.unfollowUser(sourceUserId, targetUserId),
//                ""
//        );
//
//        assertEquals(ex.getMessage(), "A user can't unfollow him/herself!");
//    }
//
//    @Test
//    public void unfollowUser_invalidSourceUserId_throwsException() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = 2;
//
//        when(userJpaRepository.findById(sourceUserId))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        EntityNotFoundException ex = assertThrows(
//                EntityNotFoundException.class,
//                () -> userService.unfollowUser(sourceUserId, targetUserId),
//                ""
//        );
//
//        var expectedMessage = new EntityNotFoundException(UserEntity.class, sourceUserId).getMessage();
//
//        assertEquals(ex.getMessage(), expectedMessage);
//    }
//
//    @Test
//    public void unfollowUser_invalidTargetUserId_throwsException() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = 2;
//
//        when(userJpaRepository.findById(sourceUserId))
//                .thenReturn(Optional.of(new UserEntity()));
//        when(userJpaRepository.findById(targetUserId))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        EntityNotFoundException ex = assertThrows(
//                EntityNotFoundException.class,
//                () -> userService.unfollowUser(sourceUserId, targetUserId),
//                ""
//        );
//
//        var expectedMessage = new EntityNotFoundException(UserEntity.class, targetUserId).getMessage();
//
//        assertEquals(ex.getMessage(), expectedMessage);
//    }
//
//    @Test
//    public void unfollowUser_targetUserNotFollowed_doesNothing() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = 2;
//
//        var sourceUser = new UserEntity();
//        sourceUser.setId(sourceUserId);
//
//        var targetUser = new UserEntity();
//        targetUser.setId(targetUserId);
//
//        when(userJpaRepository.findById(sourceUserId))
//                .thenReturn(Optional.of(sourceUser));
//        when(userJpaRepository.findById(targetUserId))
//                .thenReturn(Optional.of(targetUser));
//
//        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
//                .thenReturn(null);
//
//        // Act
//        userService.unfollowUser(sourceUserId, targetUserId);
//
//        // Assert
//        verify(followerJpaRepository, never())
//                .delete(any());
//    }
//
//    @Test
//    public void unfollowUser_validInput_deletesData() {
//        // Arrange
//        var sourceUserId = 1;
//        var targetUserId = 2;
//
//        var sourceUser = new UserEntity();
//        sourceUser.setId(sourceUserId);
//
//        var targetUser = new UserEntity();
//        targetUser.setId(targetUserId);
//
//        when(userJpaRepository.findById(sourceUserId))
//                .thenReturn(Optional.of(sourceUser));
//        when(userJpaRepository.findById(targetUserId))
//                .thenReturn(Optional.of(targetUser));
//
//        var follower = new FollowerEntity(sourceUser, targetUser);
//
//        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUser, targetUser))
//                .thenReturn(follower);
//
//        // Act
//        userService.unfollowUser(sourceUserId, targetUserId);
//
//        // Assert
//        verify(followerJpaRepository, times(1))
//                .delete(follower);
//    }
}
