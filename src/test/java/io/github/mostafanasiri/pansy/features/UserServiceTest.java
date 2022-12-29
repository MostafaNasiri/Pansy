package io.github.mostafanasiri.pansy.features;

import io.github.mostafanasiri.pansy.features.user.UserService;
import io.github.mostafanasiri.pansy.features.user.repo.FollowerRepository;
import io.github.mostafanasiri.pansy.features.user.repo.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowerRepository followerRepository;

    @InjectMocks
    private UserService userService;
}
