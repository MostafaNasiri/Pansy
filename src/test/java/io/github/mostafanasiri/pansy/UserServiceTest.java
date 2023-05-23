package io.github.mostafanasiri.pansy;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.FileEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.FollowerEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.app.data.entity.redis.UserRedis;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FeedJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FileJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FollowerJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.app.domain.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.app.domain.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.app.domain.mapper.UserDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.Image;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.service.FeedService;
import io.github.mostafanasiri.pansy.app.domain.service.FileService;
import io.github.mostafanasiri.pansy.app.domain.service.NotificationService;
import io.github.mostafanasiri.pansy.app.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest extends BaseServiceTest {
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
        verify(userJpaRepository, never())
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
        verify(userJpaRepository, never())
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

    @Test
    public void createUser_duplicateUsername_throwsException() {
        // Arrange
        var user = new User("name", "username", "123");

        when(userJpaRepository.findByUsername(user.username()))
                .thenReturn(Optional.of(new UserEntity()));

        // Act & Assert
        var ex = assertThrows(
                InvalidInputException.class,
                () -> service.createUser(user),
                ""
        );

        assertEquals(ex.getMessage(), "Username already exists");
    }

    @Test
    public void createUser_successful_createsFeedForUser() {
        // Arrange
        var user = new User("", "", "");

        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(0));

        // Act
        service.createUser(user);

        // Assert
        verify(feedJpaRepository)
                .save(any());
    }

    @Test
    public void createUser_successful_savesCreatedUserInRedis() {
        // Arrange
        var user = new User("", "", "");

        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(0));

        // Act
        service.createUser(user);

        // Assert
        verify(userRedisRepository)
                .save(any());
    }

    @Test
    public void createUser_successful_returnsCreatedUser() {
        // Arrange
        var input = new User("", "", "");

        var user = new User(0);
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(user);

        // Act
        var result = service.createUser(input);

        // Assert
        assertEquals(user, result);
    }

    @Test
    public void updateUser_inputNotBelongingToAuthenticatedUser_throwsException() {
        // Arrange
        var input = new User(AUTHENTICATED_USER_ID * 2, null, null, null);

        // Act & Assert
        var ex = assertThrows(
                AuthorizationException.class,
                () -> service.updateUser(input),
                ""
        );

        assertEquals(ex.getMessage(), "Forbidden action");
    }

    @Test
    public void updateUser_avatarNotNull_setsAvatarForUser() {
        // Arrange
        var input = new User(AUTHENTICATED_USER_ID, null, new Image(1), null);

        var authenticatedUserEntity = Mockito.mock(UserEntity.class);

        when(userJpaRepository.findById(AUTHENTICATED_USER_ID))
                .thenReturn(Optional.of(authenticatedUserEntity));
        when(fileJpaRepository.findById(any()))
                .thenReturn(Optional.of(new FileEntity()));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(input.id()));

        // Act
        service.updateUser(input);

        // Service
        verify(authenticatedUserEntity)
                .setAvatar(any());
    }

    @Test
    public void updateUser_successful_updatesUserData() {
        // Arrange
        var input = new User(AUTHENTICATED_USER_ID, "", null, "");

        var authenticatedUserEntity = Mockito.mock(UserEntity.class);

        when(userJpaRepository.findById(AUTHENTICATED_USER_ID))
                .thenReturn(Optional.of(authenticatedUserEntity));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(input.id()));

        // Act
        service.updateUser(input);

        // Assert
        verify(authenticatedUserEntity)
                .setFullName(input.fullName());
        verify(authenticatedUserEntity)
                .setBio(input.bio());
        verify(userJpaRepository)
                .save(authenticatedUserEntity);
    }

    @Test
    public void updateUser_successful_savesUserInRedis() {
        // Arrange
        var input = new User(AUTHENTICATED_USER_ID, "", null, "");

        var authenticatedUserEntity = Mockito.mock(UserEntity.class);

        when(userJpaRepository.findById(AUTHENTICATED_USER_ID))
                .thenReturn(Optional.of(authenticatedUserEntity));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(input.id()));

        // Act
        service.updateUser(input);

        // Assert
        verify(userRedisRepository)
                .save(any());
    }

    @Test
    public void updateUser_successful_returnsUpdatedUser() {
        // Arrange
        var input = new User(AUTHENTICATED_USER_ID, "", null, "");

        var authenticatedUserEntity = Mockito.mock(UserEntity.class);

        when(userJpaRepository.findById(AUTHENTICATED_USER_ID))
                .thenReturn(Optional.of(authenticatedUserEntity));

        var user = new User(input.id());
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(user);

        // Act
        var result = service.updateUser(input);

        // Assert
        assertEquals(user, result);
    }

    @Test
    public void updateUserPostCount_successful_updatesUserData() {
        // Arrange
        var userId = 1;
        var count = 2;

        var userEntity = Mockito.mock(UserEntity.class);

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(userId));

        // Act
        service.updateUserPostCount(userId, count);

        // Assert
        verify(userEntity)
                .setPostCount(count);
        verify(userJpaRepository)
                .save(userEntity);
    }

    @Test
    public void updateUserPostCount_successful_savesUserInRedis() {
        // Arrange
        var userId = 1;
        var count = 2;

        var userEntity = Mockito.mock(UserEntity.class);

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(userId));

        // Act
        service.updateUserPostCount(userId, count);

        // Assert
        verify(userRedisRepository)
                .save(any());
    }

    @Test
    public void getFollowers_validInput_usesInputsCorrectly() {
        // Arrange
        var userId = 1;
        var page = 0;
        var size = 10;

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(new UserEntity()));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(userId));

        // Act
        service.getFollowers(userId, page, size);

        // Assert
        verify(followerJpaRepository)
                .getFollowerIds(userId, PageRequest.of(page, size));
    }

    @Test
    public void getFollowing_validInput_usesInputsCorrectly() {
        // Arrange
        var userId = 1;
        var page = 0;
        var size = 10;

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(new UserEntity()));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(userId));

        // Act
        service.getFollowing(userId, page, size);

        // Assert
        verify(followerJpaRepository)
                .getFollowingIds(userId, PageRequest.of(page, size));
    }

    @Test
    public void followUser_sourceUserIdNotEqualToAuthenticatedUserId_throwsException() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID * 2;
        var targetUserId = sourceUserId * 2;

        // Act & Assert
        var ex = assertThrows(
                AuthorizationException.class,
                () -> service.followUser(sourceUserId, targetUserId),
                ""
        );

        assertEquals(ex.getMessage(), "Forbidden action");
    }

    @Test
    public void followUser_sourceUserIdSameAsTargetUserId_throwsException() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;

        // Act & Assert
        var ex = assertThrows(
                InvalidInputException.class,
                () -> service.followUser(sourceUserId, sourceUserId),
                ""
        );

        assertEquals(ex.getMessage(), "A user can't follow him/herself!");
    }

    @Test
    public void followUser_successful_updatesDatabase() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;
        var targetUserId = 2;

        when(userJpaRepository.findById(any()))
                .thenReturn(Optional.of(new UserEntity()));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(0));
        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId))
                .thenReturn(Optional.empty());

        // Act
        service.followUser(sourceUserId, targetUserId);

        // Assert
        verify(userJpaRepository, times(2))
                .save(any());
    }

    @Test
    public void followUser_successful_addsFollowNotification() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;
        var targetUserId = 2;

        when(userJpaRepository.findById(any()))
                .thenReturn(Optional.of(new UserEntity()));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(0));
        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId))
                .thenReturn(Optional.empty());

        // Act
        service.followUser(sourceUserId, targetUserId);

        // Assert
        verify(notificationService)
                .addFollowNotification(any());
    }

    @Test
    public void followUser_successful_updatesFollowingFollowerCount() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;
        var targetUserId = 2;

        when(userJpaRepository.findById(any()))
                .thenReturn(Optional.of(new UserEntity()));
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(0));
        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId))
                .thenReturn(Optional.empty());

        // Act
        service.followUser(sourceUserId, targetUserId);

        // Assert
        verify(followerJpaRepository)
                .save(any());
    }

    @Test
    public void followUser_targetUserAlreadyFollowed_doesNothing() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;
        var targetUserId = 2;

        when(userJpaRepository.findById(any()))
                .thenReturn(Optional.of(new UserEntity()));
        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId))
                .thenReturn(Optional.of(new FollowerEntity()));

        // Act
        service.followUser(sourceUserId, targetUserId);

        // Assert
        verify(followerJpaRepository, never())
                .save(any());
    }

    @Test
    public void unfollowUser_sourceUserIdNotEqualToAuthenticatedUserId_throwsException() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID * 2;
        var targetUserId = sourceUserId * 2;

        // Act & Assert
        var ex = assertThrows(
                AuthorizationException.class,
                () -> service.unfollowUser(sourceUserId, targetUserId),
                ""
        );

        assertEquals(ex.getMessage(), "Forbidden action");
    }

    @Test
    public void unfollowUser_sourceUserIdSameAsTargetUserId_throwsException() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;

        // Act & Assert
        var ex = assertThrows(
                InvalidInputException.class,
                () -> service.unfollowUser(sourceUserId, sourceUserId),
                ""
        );

        assertEquals(ex.getMessage(), "A user can't unfollow him/herself!");
    }

    @Test
    public void unfollowUser_targetUserNotFollowed_doesNothing() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;
        var targetUserId = 2;

        when(userJpaRepository.findById(any()))
                .thenReturn(Optional.of(new UserEntity()));
        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId))
                .thenReturn(Optional.empty());

        // Act
        service.unfollowUser(sourceUserId, targetUserId);

        // Assert
        verify(followerJpaRepository, never())
                .delete(any());
    }

    @Test
    public void unfollowUser_successful_updatesDatabase() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;
        var targetUserId = 2;

        when(userJpaRepository.findById(any()))
                .thenReturn(Optional.of(new UserEntity()));
        when(userJpaRepository.save(any()))
                .thenReturn(new UserEntity());
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(0));
        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId))
                .thenReturn(Optional.of(new FollowerEntity()));

        // Act
        service.unfollowUser(sourceUserId, targetUserId);

        // Assert
        verify(followerJpaRepository)
                .delete(any());
    }

    @Test
    public void unfollowUser_successful_deletesFollowNotification() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;
        var targetUserId = 2;

        when(userJpaRepository.findById(any()))
                .thenReturn(Optional.of(new UserEntity()));
        when(userJpaRepository.save(any()))
                .thenReturn(new UserEntity());
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(0));
        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId))
                .thenReturn(Optional.of(new FollowerEntity()));

        // Act
        service.unfollowUser(sourceUserId, targetUserId);

        // Assert
        verify(notificationService)
                .deleteFollowNotification(sourceUserId, targetUserId);
    }

    @Test
    public void unfollowUser_successful_removesAllPostsFromFollowerFeed() {
        // Arrange
        var sourceUserId = AUTHENTICATED_USER_ID;
        var targetUserId = 2;

        when(userJpaRepository.findById(any()))
                .thenReturn(Optional.of(new UserEntity()));
        when(userJpaRepository.save(any()))
                .thenReturn(new UserEntity());
        when(userDomainMapper.userEntityToUser(any()))
                .thenReturn(new User(0));
        when(followerJpaRepository.findBySourceUserAndTargetUser(sourceUserId, targetUserId))
                .thenReturn(Optional.of(new FollowerEntity()));

        // Act
        service.unfollowUser(sourceUserId, targetUserId);

        // Assert
        verify(feedService)
                .removeAllPostsFromFeed(sourceUserId, targetUserId);
    }
}
