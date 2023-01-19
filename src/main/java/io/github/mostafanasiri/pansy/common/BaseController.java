package io.github.mostafanasiri.pansy.common;

import io.github.mostafanasiri.pansy.auth.PansyUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {
    protected int getCurrentUserId() {
        return ((PansyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUser()
                .getId();
    }
}
