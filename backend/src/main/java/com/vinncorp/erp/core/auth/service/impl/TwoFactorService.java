package com.vinncorp.erp.core.auth.service.impl;

import com.vinncorp.erp.core.auth.entity.UserTwoFactor;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.auth.repository.TwoFactorRepository;
import com.vinncorp.erp.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base32;

@Service
public class TwoFactorService {

    @Autowired
    private TwoFactorRepository twoFactorRepository;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    // Generate secret key and return with QR code URL
    public TwoFactorSetupData generateSecret(User user) {
        // Generate 20-byte secret (standard for TOTP - 160 bits)
        byte[] secretBytes = new byte[20];
        new SecureRandom().nextBytes(secretBytes);
        // Use Base32 for Google Authenticator compatibility
        Base32 base32 = new Base32();
        String secretKey = base32.encodeAsString(secretBytes).replace("-", "").trim();

        // Save secret (but not enabled yet - user must verify first)
        UserTwoFactor twoFactor = twoFactorRepository.findByUserId(user.getId())
                .orElse(new UserTwoFactor());

        twoFactor.setUser(user);
        twoFactor.setSecretKey(secretKey);
        twoFactor.setEnabled(false);
        twoFactor.setBackupCodes(generateBackupCodes());
        twoFactorRepository.save(twoFactor);

        // Generate TOTP auth URL (use Base32 secret)
        String totpAuthURL = "otpauth://totp/PMT-SK:" + user.getEmail() + "?secret=" + secretKey + "&issuer=PMT-SK";

        // Generate QR code URL
        String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" 
                + java.net.URLEncoder.encode(totpAuthURL, StandardCharsets.UTF_8);

        return new TwoFactorSetupData(secretKey, qrCodeUrl, totpAuthURL);
    }

    // Validate TOTP code during setup (to enable 2FA)
    public boolean validateSetupCode(User user, String code) {
        Optional<UserTwoFactor> twoFactorOpt = twoFactorRepository.findByUserId(user.getId());
        if (twoFactorOpt.isEmpty()) {
            return false;
        }

        UserTwoFactor twoFactor = twoFactorOpt.get();
        boolean isValid = validateTOTP(twoFactor.getSecretKey(), code);

        if (isValid) {
            twoFactor.setEnabled(true);
            twoFactorRepository.save(twoFactor);
        }

        return isValid;
    }

    // Validate TOTP code during login
    public boolean validateCode(User user, String code) {
        Optional<UserTwoFactor> twoFactorOpt = twoFactorRepository.findByUserId(user.getId());
        if (twoFactorOpt.isEmpty() || !twoFactorOpt.get().isEnabled()) {
            return false;
        }

        UserTwoFactor twoFactor = twoFactorOpt.get();

        // Try TOTP first
        if (validateTOTP(twoFactor.getSecretKey(), code)) {
            return true;
        }

        // Try backup codes
        return validateBackupCode(twoFactor, code);
    }

    // Core TOTP validation using HMAC-SHA1
    private boolean validateTOTP(String secretKey, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            // Decode Base32 secret (Google Authenticator format)
            Base32 base32 = new Base32();
            byte[] secret = base32.decode(secretKey.trim());

            // Check current time step and one before/after (for clock drift)
            long timeStep = System.currentTimeMillis() / 30000;
            for (int i = -1; i <= 1; i++) {
                if (generateTOTP(secret, timeStep + i) == codeInt) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Generate TOTP value
    private int generateTOTP(byte[] secret, long timeStep) throws Exception {
        // Convert time step to bytes (8 bytes, big-endian)
        byte[] timeBytes = new byte[8];
        timeBytes[4] = (byte) (timeStep >> 24);
        timeBytes[5] = (byte) (timeStep >> 16);
        timeBytes[6] = (byte) (timeStep >> 8);
        timeBytes[7] = (byte) timeStep;

        // HMAC-SHA1
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA1");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(timeBytes);

        // Dynamic truncation
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                ((hash[offset + 1] & 0xFF) << 16) |
                ((hash[offset + 2] & 0xFF) << 8) |
                (hash[offset + 3] & 0xFF);

        return binary % 1000000; // 6-digit code
    }

    // Validate and consume a backup code
    private boolean validateBackupCode(UserTwoFactor twoFactor, String code) {
        if (twoFactor.getBackupCodes() == null || twoFactor.getBackupCodes().isEmpty()) {
            return false;
        }

        String[] codes = twoFactor.getBackupCodes().split(",");
        for (int i = 0; i < codes.length; i++) {
            if (passwordEncoder.matches(code, codes[i])) {
                // Remove used backup code
                List<String> remainingCodes = new ArrayList<>();
                for (int j = 0; j < codes.length; j++) {
                    if (j != i) {
                        remainingCodes.add(codes[j]);
                    }
                }
                twoFactor.setBackupCodes(String.join(",", remainingCodes));
                twoFactorRepository.save(twoFactor);
                return true;
            }
        }
        return false;
    }

    // Disable 2FA
    public void disableTwoFactor(User user) {
        twoFactorRepository.findByUserId(user.getId()).ifPresent(twoFactor -> {
            twoFactorRepository.delete(twoFactor);
        });
    }

    // Check if 2FA is enabled for user
    public boolean isTwoFactorEnabled(User user) {
        return twoFactorRepository.existsByUserIdAndEnabledTrue(user.getId());
    }

    // Get backup codes (plain, for display after setup)
    public List<String> getPlainBackupCodes(User user) {
        return generatePlainBackupCodes();
    }

    // Generate 10 backup codes (hashed for storage, plain for display)
    private String generateBackupCodes() {
        List<String> plainCodes = generatePlainBackupCodes();
        String hashedCodes = plainCodes.stream()
                .map(passwordEncoder::encode)
                .collect(Collectors.joining(","));
        return hashedCodes;
    }

    private List<String> generatePlainBackupCodes() {
        List<String> codes = new ArrayList<>();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 10; i++) {
            codes.add(String.format("%05d", random.nextInt(100000)) + String.format("%05d", random.nextInt(100000)));
        }
        return codes;
    }

    // Inner class for setup data
    public static class TwoFactorSetupData {
        private final String secretKey;
        private final String qrCodeUrl;
        private final String totpAuthUrl;

        public TwoFactorSetupData(String secretKey, String qrCodeUrl, String totpAuthUrl) {
            this.secretKey = secretKey;
            this.qrCodeUrl = qrCodeUrl;
            this.totpAuthUrl = totpAuthUrl;
        }

        public String getSecretKey() { return secretKey; }
        public String getQrCodeUrl() { return qrCodeUrl; }
        public String getTotpAuthUrl() { return totpAuthUrl; }
    }
}
