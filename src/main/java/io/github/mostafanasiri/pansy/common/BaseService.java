package io.github.mostafanasiri.pansy.common;

import io.github.mostafanasiri.pansy.auth.AppUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseService {
    protected int getAuthenticatedUserId() {
        return ((AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUser()
                .getId();
    }
}