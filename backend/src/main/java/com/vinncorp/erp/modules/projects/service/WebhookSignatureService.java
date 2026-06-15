package com.vinncorp.erp.modules.projects.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@Slf4j
public class WebhookSignatureService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final long MAX_TIMESTAMP_DIFF_MS = 5 * 60 * 1000;

    public boolean verifySignature(String payload, String signatureHeader, String secret) {
        try {
            String expectedSignature = computeSignature(payload, secret);
            return constantTimeEquals(signatureHeader, expectedSignature);
        } catch (Exception e) {
            log.error("Failed to verify webhook signature", e);
            return false;
        }
    }

    public boolean verifyTimestamp(String timestampHeader) {
        try {
            long timestamp = Long.parseLong(timestampHeader);
            long currentTime = System.currentTimeMillis();
            return Math.abs(currentTime - timestamp) <= MAX_TIMESTAMP_DIFF_MS;
        } catch (NumberFormatException e) {
            log.warn("Invalid timestamp header: {}", timestampHeader);
            return false;
        }
    }

    private String computeSignature(String payload, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}



