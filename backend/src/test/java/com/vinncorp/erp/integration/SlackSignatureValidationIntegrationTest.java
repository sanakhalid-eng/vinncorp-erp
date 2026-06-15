package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SlackSignatureValidationIntegrationTest extends AbstractIntegrationTest {

    private static final String SIGNING_SECRET = "test-signing-secret";
    private String validTimestamp;
    private String validBody;
    private String validSignature;

    @BeforeEach
    void setUp() throws Exception {
        validTimestamp = String.valueOf(Instant.now().getEpochSecond());
        validBody = "{\"event\":\"test\",\"challenge\":\"abc123\"}";
        validSignature = computeSignature(validTimestamp, validBody);
    }

    @Test
    void verifySignature_withValidInputs_shouldReturnTrue() {
        boolean result = slackService.verifySignature(validBody, validTimestamp, "v0=" + validSignature);
        assertTrue(result);
    }

    @Test
    void verifySignature_withInvalidSignature_shouldReturnFalse() {
        boolean result = slackService.verifySignature(validBody, validTimestamp, "v0=invalidsignature");
        assertFalse(result);
    }

    @Test
    void verifySignature_withExpiredTimestamp_shouldReturnFalse() {
        String oldTimestamp = String.valueOf(Instant.now().getEpochSecond() - 600);
        String sig = computeSignature(oldTimestamp, validBody);
        boolean result = slackService.verifySignature(validBody, oldTimestamp, "v0=" + sig);
        assertFalse(result);
    }

    @Test
    void verifySignature_withTamperedBody_shouldReturnFalse() {
        String tamperedBody = "{\"event\":\"hacked\",\"challenge\":\"xyz\"}";
        boolean result = slackService.verifySignature(tamperedBody, validTimestamp, "v0=" + validSignature);
        assertFalse(result);
    }

    @Test
    void verifySignature_withNullTimestamp_shouldReturnFalse() {
        boolean result = slackService.verifySignature(validBody, null, "v0=" + validSignature);
        assertFalse(result);
    }

    @Test
    void verifySignature_withNullSignature_shouldReturnFalse() {
        boolean result = slackService.verifySignature(validBody, validTimestamp, null);
        assertFalse(result);
    }

    @Test
    void verifySignature_withEmptyBody_shouldNotThrow() {
        String sig = computeSignature(validTimestamp, "");
        boolean result = slackService.verifySignature("", validTimestamp, "v0=" + sig);
        assertFalse(result);
    }

    @Test
    void verifySignature_withFutureTimestamp_shouldReturnFalse() {
        String futureTimestamp = String.valueOf(Instant.now().getEpochSecond() + 600);
        String sig = computeSignature(futureTimestamp, validBody);
        boolean result = slackService.verifySignature(validBody, futureTimestamp, "v0=" + sig);
        assertFalse(result);
    }

    @Test
    void verifySignature_withoutV0Prefix_shouldReturnFalse() {
        boolean result = slackService.verifySignature(validBody, validTimestamp, validSignature);
        assertFalse(result);
    }

    private String computeSignature(String timestamp, String body) {
        try {
            String baseString = "v0:" + timestamp + ":" + body;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(SIGNING_SECRET.getBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(baseString.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

