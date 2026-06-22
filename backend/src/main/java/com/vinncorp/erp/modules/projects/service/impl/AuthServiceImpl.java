package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.auth.dto.request.LoginRequest;
import com.vinncorp.erp.platform.auth.dto.response.LoginResponse;
import com.vinncorp.erp.platform.auth.entity.EmailVerificationToken;
import com.vinncorp.erp.platform.auth.repository.EmailVerificationTokenRepository;
import com.vinncorp.erp.platform.auth.service.AuthService;
import com.vinncorp.erp.platform.auth.service.impl.RefreshTokenService;
import com.vinncorp.erp.platform.user.dto.request.RegisterRequest;
import com.vinncorp.erp.platform.user.dto.response.RegisterResponse;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.RoleRepository;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.user.repository.UserRoleRepository;
import com.vinncorp.erp.platform.user.repository.WorkspaceUserRoleRepository;
import com.vinncorp.erp.platform.workspace.dto.response.WorkspaceSummary;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.shared.exception.EmailAlreadyExistsException;
import com.vinncorp.erp.shared.exception.ErrorCode;
import com.vinncorp.erp.shared.security.JwtUtil;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.service.BootstrapService;
import com.vinncorp.erp.modules.projects.service.EmailService;
import com.vinncorp.erp.modules.projects.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private RefreshTokenService refreshTokenService;
    @Autowired private EmailVerificationTokenRepository tokenRepository;
    @Autowired private EmailService emailService;
    @Autowired private WorkspaceMemberRepository workspaceMemberRepository;
    @Autowired private LoginAttemptService loginAttemptService;
    @Autowired private BootstrapService bootstrapService;
    @Autowired private WorkspaceUserRoleRepository workspaceUserRoleRepository;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String username = request.getUsername().trim().toLowerCase();
            if (userRepository.existsByUsername(username)) {
                throw new EmailAlreadyExistsException("Username already taken");
            }
        }

        User user = new User();
        user.setName(request.getName() != null ? request.getName() : email.split("@")[0]);
        user.setUsername(request.getUsername() != null && !request.getUsername().isBlank()
                ? request.getUsername().trim().toLowerCase()
                : email.split("@")[0].toLowerCase());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserRoles(new HashSet<>());
        user.setEmailVerified(false);
        user.setActive(true);

        User savedUser;
        if (!bootstrapService.isWorkspaceOwnerExists()) {
            savedUser = bootstrapService.createFirstUser(user);
        } else {
            savedUser = bootstrapService.createSubsequentUser(user);
        }

        String code = String.format("%06d", new Random().nextInt(999999));

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(code)
                .email(email)
                .userId(savedUser.getId())
                .expiryDate(java.time.LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();

        tokenRepository.save(token);
        emailService.sendVerificationEmail(email, code);

        return new RegisterResponse(
                "Verification code sent - please verify your email",
                email,
                Set.of()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (loginAttemptService.isLockedOut(request.getIdentifier())) {
            throw new com.vinncorp.erp.shared.exception.BadRequestException(
                    "Account temporarily locked due to too many failed login attempts. Try again later.",
                    ErrorCode.FORBIDDEN
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getIdentifier(),
                            request.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            assert userDetails != null;
            User user = userDetails.user();

            loginAttemptService.recordSuccessfulLogin(request.getIdentifier());

            return buildLoginResponse(user);
        } catch (org.springframework.security.core.AuthenticationException e) {
            loginAttemptService.recordFailedAttempt(request.getIdentifier());
            throw new com.vinncorp.erp.shared.exception.BadRequestException(
                    "Invalid credentials",
                    com.vinncorp.erp.shared.exception.ErrorCode.INVALID_CREDENTIALS
            );
        }
    }

    public LoginResponse buildLoginResponse(User user) {
        List<WorkspaceSummary> allWorkspaces = workspaceMemberRepository.findByUserIdAndActiveTrue(user.getId())
                .stream()
                .map(m -> WorkspaceSummary.builder()
                        .id(m.getWorkspace().getId())
                        .name(m.getWorkspace().getName())
                        .slug(m.getWorkspace().getSlug())
                        .role(m.getWorkspaceRole())
                        .build())
                .collect(Collectors.toList());

        if (allWorkspaces.isEmpty()) {
            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .workspaces(allWorkspaces)
                    .build();
        }

        if (allWorkspaces.size() == 1) {
            WorkspaceSummary ws = allWorkspaces.get(0);
            List<String> permissions = workspaceUserRoleRepository
                    .findPermissionNamesByWorkspaceAndUser(ws.getId(), user.getId());
            List<String> roles = workspaceUserRoleRepository
                    .findRoleNamesByWorkspaceAndUser(ws.getId(), user.getId());

            String accessToken = jwtUtil.generateTokenWithWorkspacePermissions(
                    user, ws.getId(), ws.getSlug(), permissions, roles);
            String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .currentWorkspace(ws)
                    .workspaces(allWorkspaces)
                    .workspaceRoles(roles)
                    .workspacePermissions(permissions)
                    .build();
        }

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .workspaces(allWorkspaces)
                .build();
    }

    public LoginResponse selectWorkspace(User user, Long workspaceId, String workspaceSlug) {
        List<String> permissions = workspaceUserRoleRepository
                .findPermissionNamesByWorkspaceAndUser(workspaceId, user.getId());
        List<String> roles = workspaceUserRoleRepository
                .findRoleNamesByWorkspaceAndUser(workspaceId, user.getId());

        String accessToken = jwtUtil.generateTokenWithWorkspacePermissions(
                user, workspaceId, workspaceSlug, permissions, roles);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

        WorkspaceSummary ws = WorkspaceSummary.builder()
                .id(workspaceId)
                .slug(workspaceSlug)
                .role(roles.isEmpty() ? "USER" : roles.get(0))
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .currentWorkspace(ws)
                .workspaceRoles(roles)
                .workspacePermissions(permissions)
                .build();
    }
}
