package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.shared.security.JwtUtil;
import com.vinncorp.erp.platform.auth.service.impl.RefreshTokenService;
import com.vinncorp.erp.platform.auth.service.impl.TwoFactorService;
import com.vinncorp.erp.platform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
@Tag(name = "Authentication")
public class TwoFactorController {

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    // Step 1: Generate secret and QR code (user must be logged in)
    @PostMapping("/setup")
    @Operation(summary = "Setup 2FA", description = "Generate secret key and QR code for 2FA setup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setupTwoFactor(HttpServletRequest request) {
        String email = jwtUtil.extractEmail(request.getHeader("Authorization").replace("Bearer ", ""));
        User user = userService.getUserByEmail(email);

        TwoFactorService.TwoFactorSetupData setupData = twoFactorService.generateSecret(user);

        Map<String, Object> data = new HashMap<>();
        data.put("secretKey", setupData.getSecretKey());
        data.put("qrCodeUrl", setupData.getQrCodeUrl());
        data.put("totpAuthUrl", setupData.getTotpAuthUrl());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Scan QR code with Google Authenticator", data)
        );
    }

    // Step 2: Verify code to enable 2FA
    @PostMapping("/verify-setup")
    @Operation(summary = "Verify 2FA setup", description = "Verify TOTP code to enable 2FA")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifySetup(
            HttpServletRequest request,
            @RequestBody Map<String, String> body
    ) {
        String email = jwtUtil.extractEmail(request.getHeader("Authorization").replace("Bearer ", ""));
        User user = userService.getUserByEmail(email);

        String code = body.get("code");
        boolean isValid = twoFactorService.validateSetupCode(user, code);

        if (!isValid) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid verification code", null));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("backupCodes", twoFactorService.getPlainBackupCodes(user));
        data.put("message", "2FA enabled successfully. Save your backup codes!");

        return ResponseEntity.ok(
                new ApiResponse<>(true, "2FA enabled successfully", data)
        );
    }

    // Disable 2FA
    @PostMapping("/disable")
    @Operation(summary = "Disable 2FA", description = "Disable two-factor authentication for the current user")
    public ResponseEntity<ApiResponse<?>> disableTwoFactor(HttpServletRequest request) {
        String email = jwtUtil.extractEmail(request.getHeader("Authorization").replace("Bearer ", ""));
        User user = userService.getUserByEmail(email);

        twoFactorService.disableTwoFactor(user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "2FA disabled successfully", null)
        );
    }

    // Check if 2FA is enabled
    @GetMapping("/status")
    @Operation(summary = "Get 2FA status", description = "Check if 2FA is enabled for the current user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(HttpServletRequest request) {
        String email = jwtUtil.extractEmail(request.getHeader("Authorization").replace("Bearer ", ""));
        User user = userService.getUserByEmail(email);

        boolean enabled = twoFactorService.isTwoFactorEnabled(user);

        Map<String, Object> data = new HashMap<>();
        data.put("enabled", enabled);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "2FA status", data)
        );
    }

    @Autowired
    private RefreshTokenService refreshTokenService;

    // Validate 2FA code during login
    @PostMapping("/validate")
    @Operation(summary = "Validate 2FA", description = "Validate TOTP code during login and return tokens")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateTwoFactor(
            @RequestBody Map<String, String> body
    ) {
        String identifier = body.get("identifier");
        String code = body.get("code");

        User user = userService.getUserByEmailOrUsername(identifier);

        boolean isValid = twoFactorService.validateCode(user, code);

        if (!isValid) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid 2FA code", null));
        }

        // Generate tokens after successful 2FA
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService
                .createRefreshToken(user.getEmail())
                .getToken();

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("userId", user.getId());
        data.put("message", "2FA verification successful");

        return ResponseEntity.ok(
                new ApiResponse<>(true, "2FA verified", data)
        );
    }
}


