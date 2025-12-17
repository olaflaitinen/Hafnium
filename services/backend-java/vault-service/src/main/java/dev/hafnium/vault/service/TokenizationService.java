package dev.hafnium.vault.service;

import dev.hafnium.common.security.TenantContext;
import dev.hafnium.vault.dto.TokenizeRequest;
import dev.hafnium.vault.dto.TokenizeResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for PII tokenization.
 *
 * <p>
 * Provides tokenization and detokenization of sensitive data. In production,
 * this would
 * integrate with HashiCorp Vault or similar secret management solution.
 */
@Service
public class TokenizationService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenizationService.class);

    // In-memory token store for development (replace with persistent store in
    // production)
    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    /**
     * Tokenizes sensitive data.
     *
     * @param request The tokenization request
     * @return Response with tokens for each field
     */
    public TokenizeResponse tokenize(TokenizeRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        Map<String, String> tokens = new HashMap<>();

        for (Map.Entry<String, String> entry : request.data().entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue();

            String token = generateToken(tenantId, fieldName, value);
            tokens.put(fieldName, token);

            // Store mapping
            tokenStore.put(
                    token,
                    new TokenEntry(tenantId, fieldName, value, request.dataType(), request.retentionDays()));

            LOG.debug("Tokenized field {} for tenant {}", fieldName, tenantId);
        }

        return new TokenizeResponse(tokens);
    }

    /**
     * Detokenizes a single token.
     *
     * @param token The token to detokenize
     * @return The original value, or null if not found or unauthorized
     */
    public String detokenize(String token) {
        UUID tenantId = TenantContext.requireTenantId();

        TokenEntry entry = tokenStore.get(token);
        if (entry == null) {
            LOG.warn("Token not found: {}", token);
            return null;
        }

        if (!entry.tenantId().equals(tenantId)) {
            LOG.warn("Token tenant mismatch for token {}", token);
            return null;
        }

        LOG.debug("Detokenized field {} for tenant {}", entry.fieldName(), tenantId);
        return entry.value();
    }

    /**
     * Detokenizes multiple tokens.
     *
     * @param tokens Map of field names to tokens
     * @return Map of field names to original values
     */
    public Map<String, String> detokenizeAll(Map<String, String> tokens) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            String value = detokenize(entry.getValue());
            if (value != null) {
                result.put(entry.getKey(), value);
            }
        }

        return result;
    }

    /**
     * Deletes a token and its stored value.
     *
     * @param token The token to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteToken(String token) {
        UUID tenantId = TenantContext.requireTenantId();

        TokenEntry entry = tokenStore.get(token);
        if (entry == null || !entry.tenantId().equals(tenantId)) {
            return false;
        }

        tokenStore.remove(token);
        LOG.info("Deleted token for tenant {}", tenantId);
        return true;
    }

    private String generateToken(UUID tenantId, String fieldName, String value) {
        try {
            String input = tenantId + "|" + fieldName + "|" + value + "|" + UUID.randomUUID();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Use URL-safe base64 encoding for token
            return "tok_" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private record TokenEntry(
            UUID tenantId, String fieldName, String value, String dataType, int retentionDays) {
    }
}
