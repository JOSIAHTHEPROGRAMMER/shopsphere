package com.app.shopsphere.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Provides accessors for the currently authenticated user and role information.
 */
public class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long getCurrentUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new IllegalStateException("No authenticated user found");
        }

        return (Long) authentication.getPrincipal();
    }

    public static boolean hasRole(String role) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}