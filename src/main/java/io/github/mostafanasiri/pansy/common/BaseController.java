package io.github.mostafanasiri.pansy.common;

import io.github.mostafanasiri.pansy.auth.PansyUserDetails;
import io.github.mostafanasiri.pansy.features.user.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {
    protected User getCurrentUser() {
        return ((PansyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }
}
