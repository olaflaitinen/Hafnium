package dev.hafnium.common.security;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Converts JWT tokens to Spring Security authentication with extracted roles
 * and tenant context.
 *
 * <p>
 * Extracts:
 *
 * <ul>
 * <li>Realm roles from realm_access.roles
 * <li>Client roles from resource_access.{client}.roles
 * <li>Tenant ID from tenant_id claim
 * <li>User ID from sub claim
 * </ul>
 */
public class HafniumJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_KEY = "roles";
    private static final String TENANT_ID_CLAIM = "tenant_id";
    private static final String ROLE_PREFIX = "ROLE_";

    private final String clientId;

    public HafniumJwtAuthenticationConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        initializeTenantContext(jwt);
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extract realm roles
        authorities.addAll(extractRealmRoles(jwt));

        // Extract client roles
        authorities.addAll(extractClientRoles(jwt));

        // Extract scopes
        authorities.addAll(extractScopes(jwt));

        return authorities;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess == null) {
            return Collections.emptyList();
        }

        Object roles = realmAccess.get(ROLES_KEY);
        if (!(roles instanceof Collection<?>)) {
            return Collections.emptyList();
        }

        return ((Collection<String>) roles)
                .stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);
        if (resourceAccess == null || clientId == null) {
            return Collections.emptyList();
        }

        Object clientAccess = resourceAccess.get(clientId);
        if (!(clientAccess instanceof Map<?, ?>)) {
            return Collections.emptyList();
        }

        Object roles = ((Map<String, Object>) clientAccess).get(ROLES_KEY);
        if (!(roles instanceof Collection<?>)) {
            return Collections.emptyList();
        }

        return ((Collection<String>) roles)
                .stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .collect(Collectors.toSet());
    }

    private Collection<GrantedAuthority> extractScopes(Jwt jwt) {
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || scope.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(scope.split("\\s+"))
                .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                .collect(Collectors.toSet());
    }

    private void initializeTenantContext(Jwt jwt) {
        String tenantIdStr = jwt.getClaimAsString(TENANT_ID_CLAIM);
        UUID tenantId = tenantIdStr != null ? UUID.fromString(tenantIdStr) : null;
        UUID userId = UUID.fromString(jwt.getSubject());
        String username = jwt.getClaimAsString("preferred_username");

        if (tenantId != null) {
            TenantContext.set(new TenantContext(tenantId, userId, username));
        }
    }
}
