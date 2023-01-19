package io.github.mostafanasiri.pansy.common;

import io.github.mostafanasiri.pansy.auth.PansyUserDetails;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {
    protected UserEntity getCurrentUser() { // TODO: Change to getCurrentUserId
        return ((PansyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }
}
