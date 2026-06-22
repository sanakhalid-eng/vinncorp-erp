package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.service.BootstrapService;
import com.vinncorp.erp.modules.projects.service.EmailService;
import com.vinncorp.erp.platform.auth.dto.request.LoginRequest;
import com.vinncorp.erp.platform.auth.dto.response.LoginResponse;
import com.vinncorp.erp.platform.auth.entity.EmailVerificationToken;
import com.vinncorp.erp.platform.auth.entity.RefreshToken;
import com.vinncorp.erp.platform.auth.repository.EmailVerificationTokenRepository;
import com.vinncorp.erp.platform.auth.repository.PasswordResetTokenRepository;
import com.vinncorp.erp.platform.auth.service.AuthService;
import com.vinncorp.erp.platform.auth.service.PasswordResetService;
import com.vinncorp.erp.platform.auth.service.impl.RefreshTokenService;
import com.vinncorp.erp.platform.auth.service.impl.TwoFactorService;
import com.vinncorp.erp.platform.user.dto.request.RegisterRequest;
import com.vinncorp.erp.platform.user.dto.response.RegisterResponse;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.user.repository.WorkspaceUserRoleRepository;
import com.vinncorp.erp.platform.workspace.dto.response.WorkspaceSummary;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.shared.exception.InvalidTokenException;
import com.vinncorp.erp.shared.security.JwtUtil;
import com.vinncorp.erp.modules.projects.service.impl.AuthServiceImpl;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.shared.security.MembershipResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, register, refresh tokens, 2FA")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private EmailVerificationTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TwoFactorService twoFactorService;
    @Autowired
    private BootstrapService bootstrapService;
    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;
    @Autowired
    private WorkspaceUserRoleRepository workspaceUserRoleRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private MembershipResolver membershipResolver;
    @Autowired
    private WorkspaceRepository workspaceRepository;

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register a new user account")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authService.register(request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "User registered successfully", response)
        );
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify email address using verification code")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyEmail(
            @RequestBody Map<String, String> request
    ) {
        String code = request.get("code");
        String email = request.get("email");

        EmailVerificationToken token = tokenRepository.findByTokenAndEmailAndIsUsedFalse(code, email)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification code"));

        if (token.isExpired()) {
            throw new InvalidTokenException("Verification code has expired");
        }

        if (!token.getEmail().equals(email)) {
            throw new InvalidTokenException("Invalid verification code for this email");
        }

        // Find existing user by email (created during registration)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found. Please register again."));

        // If already verified, just generate tokens
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        // Mark token as used and delete
        token.setIsUsed(true);
        tokenRepository.save(token);
        tokenRepository.delete(token);

        WorkspaceSummary workspaceSummary = workspaceMemberRepository.findByUserIdAndActiveTrue(user.getId())
                .stream()
                .findFirst()
                .map(m -> WorkspaceSummary.builder()
                        .id(m.getWorkspace().getId())
                        .name(m.getWorkspace().getName())
                        .slug(m.getWorkspace().getSlug())
                        .role(m.getWorkspaceRole())
                        .build())
                .orElse(null);

        // Generate auth tokens with workspace context
        String accessToken = jwtUtil.generateToken(
                user,
                workspaceSummary != null ? workspaceSummary.getId() : null,
                workspaceSummary != null ? workspaceSummary.getSlug() : null
        );
        String refreshToken = refreshTokenService
                .createRefreshToken(user.getEmail())
                .getToken();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .currentWorkspace(workspaceSummary)
                .build();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Email verified successfully", loginResponse)
        );
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification", description = "Resend email verification code")
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().isEmailVerified()) {
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Email is already verified", null)
            );
        }

        // Invalidate old tokens by email
        var oldTokens = tokenRepository.findByEmailAndIsUsedFalse(email);
        oldTokens.forEach(token -> {
            token.setIsUsed(true);
            tokenRepository.save(token);
        });

        // Generate new 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));

        EmailVerificationToken.EmailVerificationTokenBuilder tokenBuilder = EmailVerificationToken.builder()
                .token(code)
                .email(email)
                .expiryDate(java.time.LocalDateTime.now().plusMinutes(15))
                .isUsed(false);

        // Link token to existing user
        userOpt.ifPresent(user -> tokenBuilder.userId(user.getId()));

        EmailVerificationToken newToken = tokenBuilder.build();
        tokenRepository.save(newToken);

        // Send verification email
        emailService.sendVerificationEmail(email, code);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "New verification code sent", null)
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user with email/username and return JWT tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        // Check if 2FA is enabled for this user
        var userOpt = userRepository.findByEmailOrUsername(request.getIdentifier(), request.getIdentifier());
        if (userOpt.isPresent() && twoFactorService.isTwoFactorEnabled(userOpt.get())) {
            // Return message indicating 2FA is required (don't send tokens yet)
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "2FA_REQUIRED", null)
            );
        }

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Login successful", response)
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token with rotation")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestBody Map<String, String> request
    ) {
        String requestToken = request.get("refreshToken");

        RefreshToken oldRefreshToken = refreshTokenService.findByToken(requestToken);

        // Verify expiration and revocation (throws if invalid)
        refreshTokenService.verifyExpiration(oldRefreshToken);

        // Rotate: create new refresh token, revoke old one
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldRefreshToken);

        User tokenUser = newRefreshToken.getUser();

        WorkspaceSummary workspaceSummary = workspaceMemberRepository.findByUserIdAndActiveTrue(tokenUser.getId())
                .stream()
                .findFirst()
                .map(m -> WorkspaceSummary.builder()
                        .id(m.getWorkspace().getId())
                        .name(m.getWorkspace().getName())
                        .slug(m.getWorkspace().getSlug())
                        .role(m.getWorkspaceRole())
                        .build())
                .orElse(null);

        List<String> workspacePermissions = new ArrayList<>();
        List<String> workspaceRoles = new ArrayList<>();

        if (workspaceSummary != null) {
            workspacePermissions = workspaceUserRoleRepository.findPermissionNamesByWorkspaceAndUser(
                    workspaceSummary.getId(), tokenUser.getId());
            workspaceRoles = workspaceUserRoleRepository.findRoleNamesByWorkspaceAndUser(
                    workspaceSummary.getId(), tokenUser.getId());
        }

        String newAccessToken;
        if (!workspacePermissions.isEmpty()) {
            newAccessToken = jwtUtil.generateTokenWithWorkspacePermissions(
                    tokenUser,
                    workspaceSummary.getId(),
                    workspaceSummary.getSlug(),
                    workspacePermissions,
                    workspaceRoles
            );
        } else {
            newAccessToken = jwtUtil.generateToken(
                    tokenUser,
                    workspaceSummary != null ? workspaceSummary.getId() : null,
                    workspaceSummary != null ? workspaceSummary.getSlug() : null
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Token refreshed",
                        LoginResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(newRefreshToken.getToken())
                                .userId(tokenUser.getId())
                                .currentWorkspace(workspaceSummary)
                                .workspacePermissions(workspacePermissions)
                                .workspaceRoles(workspaceRoles)
                                .build()
                )
        );
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email to user")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        passwordResetService.initiatePasswordReset(email);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "If an account exists with that email, a password reset link has been sent", null)
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestBody Map<String, String> request
    ) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Password reset successfully", null)
        );
    }

    @GetMapping("/reset-password/verify")
    @Operation(summary = "Verify reset token", description = "Verify if password reset token is valid")
    public ResponseEntity<ApiResponse<Boolean>> verifyResetToken(
            @RequestParam String token
    ) {
        boolean isValid = passwordResetService.verifyToken(token);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Token verified", isValid)
        );
    }

    @PostMapping("/select-workspace")
    @Operation(summary = "Select workspace", description = "Select a workspace and get a new JWT with workspace-specific permissions")
    public ResponseEntity<ApiResponse<LoginResponse>> selectWorkspace(
            @RequestBody Map<String, Object> request
    ) {
        Long workspaceId = Long.valueOf(request.get("workspaceId").toString());
        String workspaceSlug = (String) request.getOrDefault("workspaceSlug", "");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return ResponseEntity.status(401).body(
                    new ApiResponse<>(false, "Not authenticated", null)
            );
        }

        User user = userDetails.user();

        // SUPER_ADMIN can enter any workspace without membership
        boolean isSuperAdmin = membershipResolver.isSuperAdmin(user);
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, user.getId());

        if (!isSuperAdmin && !isMember) {
            return ResponseEntity.status(403).body(
                    new ApiResponse<>(false, "You are not a member of this workspace", null)
            );
        }

        if (workspaceSlug == null || workspaceSlug.isBlank()) {
            if (isMember) {
                var ws = workspaceMemberRepository.findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, user.getId());
                if (ws.isPresent()) {
                    workspaceSlug = ws.get().getWorkspace().getSlug();
                }
            } else {
                // SUPER_ADMIN: lookup workspace slug from repository
                var ws = workspaceRepository.findById(workspaceId);
                if (ws.isPresent()) {
                    workspaceSlug = ws.get().getSlug();
                }
            }
        }

        final String finalSlug = workspaceSlug;
        AuthServiceImpl authServiceImpl = (AuthServiceImpl) authService;
        LoginResponse response = authServiceImpl.selectWorkspace(user, workspaceId, finalSlug);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Workspace selected", response)
        );
    }

    @GetMapping("/my-workspaces")
    @Operation(summary = "My workspaces", description = "Get all workspaces the authenticated user belongs to")
    public ResponseEntity<ApiResponse<List<WorkspaceSummary>>> myWorkspaces() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return ResponseEntity.status(401).body(
                    new ApiResponse<>(false, "Not authenticated", null)
            );
        }

        User user = userDetails.user();

        List<WorkspaceSummary> workspaces = workspaceMemberRepository.findByUserIdAndActiveTrue(user.getId())
                .stream()
                .map(m -> WorkspaceSummary.builder()
                        .id(m.getWorkspace().getId())
                        .name(m.getWorkspace().getName())
                        .slug(m.getWorkspace().getSlug())
                        .role(m.getWorkspaceRole())
                        .build())
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Workspaces retrieved", workspaces)
        );
    }
}