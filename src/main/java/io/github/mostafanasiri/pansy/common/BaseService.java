package io.github.mostafanasiri.pansy.common;

import io.github.mostafanasiri.pansy.auth.AppUserDetails;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseService {
    protected UserEntity getAuthenticatedUser() {
        return ((AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUser();
    }

    protected int getAuthenticatedUserId() {
        return ((AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUser()
                .getId();
    }
}
