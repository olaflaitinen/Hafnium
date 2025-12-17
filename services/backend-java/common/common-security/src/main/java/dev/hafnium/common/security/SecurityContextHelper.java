package dev.hafnium.common.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Utility component for accessing security context information.
 *
 * <p>
 * Provides convenient methods for extracting user information, roles, and
 * tenant details from
 * the current security context.
 */
@Component
public class SecurityContextHelper {

    /** Role prefix used by Spring Security. */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * Gets the current authenticated user's subject (user ID).
     *
     * @return The user ID, or null if not authenticated
     */
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getSubject();
        }
        return null;
    }

    /**
     * Gets the current authenticated user's email.
     *
     * @return The email, or null if not available
     */
    public String getCurrentUserEmail() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("email") : null;
    }

    /**
     * Gets the current authenticated user's preferred username.
     *
     * @return The username, or null if not available
     */
    public String getCurrentUsername() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("preferred_username") : null;
    }

    /**
     * Gets all roles for the current authenticated user.
     *
     * @return Set of role names (without ROLE_ prefix)
     */
    public Set<String> getCurrentUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Collections.emptySet();
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()).toLowerCase())
                .collect(Collectors.toSet());
    }

    /**
     * Checks if the current user has a specific role.
     *
     * @param role The role to check (without ROLE_ prefix)
     * @return true if the user has the role
     */
    public boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role.toLowerCase());
    }

    /**
     * Checks if the current user has any of the specified roles.
     *
     * @param roles The roles to check
     * @return true if the user has at least one of the roles
     */
    public boolean hasAnyRole(String... roles) {
        Set<String> userRoles = getCurrentUserRoles();
        for (String role : roles) {
            if (userRoles.contains(role.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current authentication is a service account.
     *
     * @return true if the authentication is from a service client
     */
    public boolean isServiceAccount() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            return false;
        }
        // Keycloak service accounts have client_id but no preferred_username
        return jwt.getClaimAsString("client_id") != null
                && jwt.getClaimAsString("preferred_username") == null;
    }

    /**
     * Gets the client ID for service-to-service authentication.
     *
     * @return The client ID, or null if not a service account
     */
    public String getClientId() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("client_id") : null;
    }

    /**
     * Gets the current JWT token.
     *
     * @return The JWT, or null if not authenticated with JWT
     */
    private Jwt getCurrentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }
}
