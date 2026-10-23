package com.kiran.flightbooking.security;

import com.kiran.flightbooking.enums.Role;
import com.kiran.flightbooking.exception.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return Optional.empty();
        }
        return Optional.of((UserPrincipal) authentication.getPrincipal());
    }

    public static UserPrincipal getRequiredCurrentUserPrincipal() {
        return getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedAccessException("Current user is not authenticated"));
    }

    public static Long getCurrentUserId() {
        return getRequiredCurrentUserPrincipal().getId();
    }

    public static String getCurrentUserEmail() {
        return getRequiredCurrentUserPrincipal().getUsername();
    }

    public static Role getCurrentUserRole() {
        return getRequiredCurrentUserPrincipal().getRole();
    }

    public static boolean isCurrentUserAdmin() {
        return getCurrentUserPrincipal()
                .map(p -> p.getRole() == Role.ADMIN)
                .orElse(false);
    }
}
