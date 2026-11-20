package com.docspace.security;

import com.docspace.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Resolves the authenticated caller's identity strictly from the Spring Security
 * context (session principal), never from any client-supplied id, so service methods
 * cannot be tricked into acting as a different user.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UserPrincipal get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenException("Authentication required");
        }
        return principal;
    }

    public static UUID id() {
        return get().getId();
    }
}
