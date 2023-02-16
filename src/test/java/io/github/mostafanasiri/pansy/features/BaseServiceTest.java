package io.github.mostafanasiri.pansy.features;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.auth.AppUserDetails;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.when;

public abstract class BaseServiceTest {
    protected final static int AUTHENTICATED_USER_ID = 123;

    @BeforeAll
    protected static void setup() {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        AppUserDetails appUserDetails = Mockito.mock(AppUserDetails.class);
        when(appUserDetails.getUser())
                .thenReturn(Mockito.mock(UserEntity.class));
        when(appUserDetails.getUser().getId())
                .thenReturn(AUTHENTICATED_USER_ID);

        when(authentication.getPrincipal())
                .thenReturn(appUserDetails);
    }
}
