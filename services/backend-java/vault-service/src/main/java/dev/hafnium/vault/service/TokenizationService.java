package dev.hafnium.vault.service;

import dev.hafnium.common.authz.OpaClient;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.vault.domain.AccessLog;
import dev.hafnium.vault.domain.Token;
import dev.hafnium.vault.dto.TokenizeRequest;
import dev.hafnium.vault.dto.TokenizeResponse;
import dev.hafnium.vault.dto.DetokenizeRequest;
import dev.hafnium.vault.dto.DetokenizeResponse;
import dev.hafnium.vault.repository.AccessLogRepository;
import dev.hafnium.vault.repository.TokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tokenization service for PII protection.
 */
@Slf4j
@Service
public class TokenizationService {

    private final TokenRepository tokenRepository;
    private final AccessLogRepository accessLogRepository;
    private final OpaClient opaClient;
    private final SecretKeySpec encryptionKey;
    private final SecretKeySpec hmacKey;
    private final SecureRandom secureRandom;

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public TokenizationService(
            TokenRepository tokenRepository,
            AccessLogRepository accessLogRepository,
            OpaClient opaClient,
            @Value("${hafnium.vault.encryption-key}") String encryptionKeyHex,
            @Value("${hafnium.vault.hmac-key}") String hmacKeyHex) {
        this.tokenRepository = tokenRepository;
        this.accessLogRepository = accessLogRepository;
        this.opaClient = opaClient;
        this.encryptionKey = new SecretKeySpec(hexToBytes(encryptionKeyHex), "AES");
        this.hmacKey = new SecretKeySpec(hexToBytes(hmacKeyHex), "HmacSHA256");
        this.secureRandom = new SecureRandom();
    }

    /**
     * Tokenizes sensitive data.
     */
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        UUID userId = TenantContext.requireUserId();

        // Generate deterministic token using HMAC
        String token = generateToken(tenantId, request.dataType(), request.value());

        // Check if token already exists
        return tokenRepository.findByTenantIdAndToken(tenantId, token)
                .map(existing -> {
                    logAccess(existing, AccessLog.Operation.TOKENIZE, userId, true, null);
                    return new TokenizeResponse(token, false);
                })
                .orElseGet(() -> {
                    // Encrypt and store
                    byte[] iv = new byte[GCM_IV_LENGTH];
                    secureRandom.nextBytes(iv);
                    byte[] encrypted = encrypt(request.value(), iv);

                    Token newToken = Token.builder()
                            .tenantId(tenantId)
                            .token(token)
                            .dataType(request.dataType())
                            .encryptedValue(encrypted)
                            .iv(iv)
                            .createdBy(userId)
                            .build();

                    tokenRepository.save(newToken);
                    logAccess(newToken, AccessLog.Operation.TOKENIZE, userId, true, null);

                    log.info("Created token: type={}", request.dataType());
                    return new TokenizeResponse(token, true);
                });
    }

    /**
     * Detokenizes a token back to the original value.
     * Requires elevated permissions.
     */
    @Transactional
    public DetokenizeResponse detokenize(DetokenizeRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        UUID userId = TenantContext.requireUserId();

        // Check OPA authorization
        if (!opaClient.isAllowed("detokenize", "vault", request.token())) {
            logAccessFailed(request.token(), userId, "Authorization denied");
            throw new AccessDeniedException("Detokenization not authorized");
        }

        Token token = tokenRepository.findByTenantIdAndToken(tenantId, request.token())
                .orElseThrow(() -> {
                    logAccessFailed(request.token(), userId, "Token not found");
                    return new IllegalArgumentException("Token not found");
                });

        try {
            String value = decrypt(token.getEncryptedValue(), token.getIv());
            token.setLastAccessedAt(java.time.Instant.now());
            token.setAccessCount(token.getAccessCount() + 1);
            tokenRepository.save(token);

            logAccess(token, AccessLog.Operation.DETOKENIZE, userId, true, request.reason());

            log.info("Detokenized: type={}, reason={}", token.getDataType(), request.reason());
            return new DetokenizeResponse(value, token.getDataType());
        } catch (Exception e) {
            logAccess(token, AccessLog.Operation.DETOKENIZE, userId, false, e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private String generateToken(UUID tenantId, String dataType, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            String input = tenantId + ":" + dataType + ":" + value;
            byte[] hash = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 32);
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    private byte[] encrypt(String plaintext, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, spec);
            return cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    private String decrypt(byte[] ciphertext, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, spec);
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private void logAccess(Token token, AccessLog.Operation operation, UUID actorId, boolean success, String reason) {
        AccessLog log = AccessLog.builder()
                .tenantId(token.getTenantId())
                .token(token)
                .operation(operation)
                .actorId(actorId)
                .reason(reason)
                .success(success)
                .build();
        accessLogRepository.save(log);
    }

    private void logAccessFailed(String tokenValue, UUID actorId, String error) {
        AccessLog log = AccessLog.builder()
                .tenantId(TenantContext.requireTenantId())
                .operation(AccessLog.Operation.DETOKENIZE)
                .actorId(actorId)
                .success(false)
                .errorMessage(error)
                .build();
        accessLogRepository.save(log);
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
