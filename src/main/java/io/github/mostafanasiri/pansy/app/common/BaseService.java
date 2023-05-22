package io.github.mostafanasiri.pansy.app.common;

import io.github.mostafanasiri.pansy.app.presentation.auth.AppUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseService {
    protected int getAuthenticatedUserId() {
        return ((AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUser()
                .getId();
    }
}
