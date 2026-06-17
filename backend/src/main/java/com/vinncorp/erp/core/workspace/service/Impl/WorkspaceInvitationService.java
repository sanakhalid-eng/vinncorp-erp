package com.vinncorp.erp.core.workspace.service.Impl;

import com.vinncorp.erp.core.user.entity.User;

import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.entity.WorkspaceInvitation;
import com.vinncorp.erp.core.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.core.workspace.enums.InvitationStatus;
import com.vinncorp.erp.core.workspace.repository.WorkspaceInvitationRepository;
import com.vinncorp.erp.core.workspace.request.CreateWorkspaceInvitationRequest;
import com.vinncorp.erp.core.workspace.response.WorkspaceInvitationResponse;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.modules.projects.service.EmailService;
import com.vinncorp.erp.modules.projects.service.EmailTemplateService;

import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.CustomAccessDeniedException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceInvitationService {

    private final WorkspaceInvitationRepository workspaceInvitationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @Transactional
    public WorkspaceInvitationResponse createInvitation(Long workspaceId, CreateWorkspaceInvitationRequest request, Long invitedByUserId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        User invitedBy = userRepository.findById(invitedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String email = request.getEmail().trim().toLowerCase();
        String role = request.getWorkspaceRole() != null ? request.getWorkspaceRole() : "WORKSPACE_MEMBER";

        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, existingUser.getId())) {
                throw new BadRequestException("User is already a member of this workspace");
            }
        });

        List<WorkspaceInvitation> existingPending = workspaceInvitationRepository
                .findByWorkspaceIdAndEmailOrderByCreatedAtDesc(workspaceId, email).stream()
                .filter(i -> i.getStatus() == InvitationStatus.PENDING)
                .toList();

        if (!existingPending.isEmpty()) {
            boolean sameRolePending = existingPending.stream()
                    .anyMatch(i -> role.equals(i.getWorkspaceRole()));
            if (sameRolePending) {
                throw new BadRequestException("A pending invitation already exists for this email with the same role");
            }
            throw new BadRequestException("A pending invitation already exists for this email");
        }

        String rawToken = UUID.randomUUID().toString();
        WorkspaceInvitation invitation = new WorkspaceInvitation();
        invitation.setWorkspace(workspace);
        invitation.setEmail(email);
        invitation.setInvitedBy(invitedBy);
        invitation.setWorkspaceRole(role);
        invitation.setToken(hashToken(rawToken));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation = workspaceInvitationRepository.save(invitation);

        sendInvitationEmail(invitation, rawToken, invitedBy.getName(), workspace.getName(), role);

        activityLogService.logActivity(
                invitedByUserId,
                EntityType.WORKSPACE,
                workspaceId,
                ActionType.WORKSPACE_INVITATION_CREATED,
                null,
                Map.of("email", email, "role", role, "workspaceId", workspaceId),
                "Invitation sent to " + email + " for workspace " + workspace.getName(),
                null
        );

        return toResponse(invitation);
    }

    @Transactional
    public WorkspaceInvitationResponse acceptInvitation(String token, Long currentUserId) {
        List<WorkspaceInvitation> allPending = workspaceInvitationRepository.findByStatus(InvitationStatus.PENDING);
        WorkspaceInvitation invitation = allPending.stream()
                .filter(i -> constantTimeEquals(hashToken(token), i.getToken()))
                .findFirst()
                .orElse(null);

        if (invitation == null) {
            if (workspaceInvitationRepository.findByToken(hashToken(token)).isPresent()) {
                throw new BadRequestException("Invitation is no longer pending");
            }
            throw new ResourceNotFoundException("Invalid or expired invitation token");
        }

        if (invitation.isExpired()) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            workspaceInvitationRepository.save(invitation);
            throw new BadRequestException("Invitation has expired");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            activityLogService.logActivity(
                    currentUserId, EntityType.WORKSPACE, invitation.getId(),
                    ActionType.SECURITY_VALIDATION_FAILED,
                    Map.of("expectedEmail", invitation.getEmail(), "attemptedEmail", user.getEmail()),
                    null, "Email mismatch on workspace invitation acceptance", null
            );
            throw new CustomAccessDeniedException("This invitation was sent to a different email address");
        }

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(
                invitation.getWorkspace().getId(), currentUserId)) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(LocalDateTime.now());
            workspaceInvitationRepository.save(invitation);
            throw new BadRequestException("You are already a member of this workspace");
        }

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(invitation.getWorkspace());
        member.setUser(user);
        member.setWorkspaceRole(invitation.getWorkspaceRole() != null ? invitation.getWorkspaceRole() : "WORKSPACE_MEMBER");
        member.setJoinedAt(LocalDateTime.now());
        member.setActive(true);
        workspaceMemberRepository.save(member);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        workspaceInvitationRepository.save(invitation);

        List<WorkspaceInvitation> otherPending = workspaceInvitationRepository
                .findByWorkspaceIdAndEmailOrderByCreatedAtDesc(invitation.getWorkspace().getId(), invitation.getEmail())
                .stream()
                .filter(i -> i.getStatus() == InvitationStatus.PENDING && !i.getId().equals(invitation.getId()))
                .toList();
        for (WorkspaceInvitation other : otherPending) {
            other.setStatus(InvitationStatus.REVOKED);
            workspaceInvitationRepository.save(other);
        }

        activityLogService.logActivity(
                currentUserId,
                EntityType.WORKSPACE,
                invitation.getWorkspace().getId(),
                ActionType.WORKSPACE_INVITATION_ACCEPTED,
                null,
                Map.of("email", user.getEmail(), "role", invitation.getWorkspaceRole(), "workspaceId", invitation.getWorkspace().getId()),
                "User " + user.getName() + " accepted invitation to workspace " + invitation.getWorkspace().getName(),
                null
        );

        return toResponse(invitation);
    }

    @Transactional
    public void revokeInvitation(Long invitationId, Long currentUserId) {
        WorkspaceInvitation invitation = workspaceInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Can only revoke pending invitations");
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        workspaceInvitationRepository.save(invitation);

        activityLogService.logActivity(
                currentUserId,
                EntityType.WORKSPACE,
                invitation.getWorkspace().getId(),
                ActionType.INVITATION_REVOKED,
                Map.of("email", invitation.getEmail(), "status", invitation.getStatus().name()),
                null,
                "Invitation revoked for " + invitation.getEmail(),
                null
        );
    }

    @Transactional(readOnly = true)
    public List<WorkspaceInvitationResponse> getWorkspaceInvitations(Long workspaceId) {
        return workspaceInvitationRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public WorkspaceInvitationResponse getInvitationByToken(String token) {
        String hashedToken = hashToken(token);
        List<WorkspaceInvitation> candidates = workspaceInvitationRepository.findByStatus(InvitationStatus.PENDING);
        WorkspaceInvitation invitation = candidates.stream()
                .filter(i -> constantTimeEquals(hashedToken, i.getToken()))
                .findFirst()
                .orElse(null);

        if (invitation == null) {
            invitation = workspaceInvitationRepository.findByToken(hashedToken)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token"));
        }

        if (invitation.isExpired() && invitation.getStatus() == InvitationStatus.PENDING) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            workspaceInvitationRepository.save(invitation);
        }

        return toResponse(invitation);
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void expireStaleInvitations() {
        List<WorkspaceInvitation> expired = workspaceInvitationRepository.findExpiredPendingInvitations();
        for (WorkspaceInvitation invitation : expired) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            workspaceInvitationRepository.save(invitation);
            log.info("Expired workspace invitation {} for {}", invitation.getId(), invitation.getEmail());
        }
        if (!expired.isEmpty()) {
            log.info("Expired {} stale workspace invitation(s)", expired.size());
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private void sendInvitationEmail(WorkspaceInvitation invitation, String rawToken, String inviterName, String workspaceName, String roleName) {
        try {
            String inviteUrl = baseUrl + "/workspace-invite/" + rawToken;
            String expirationDate = invitation.getExpiresAt().toLocalDate().toString();

            Map<String, Object> variables = new HashMap<>();
            variables.put("inviterName", inviterName);
            variables.put("projectName", workspaceName);
            variables.put("roleName", roleName);
            variables.put("inviteUrl", inviteUrl);
            variables.put("expirationDate", expirationDate);

            String htmlContent = emailTemplateService.loadTemplate("invitation.html", variables);

            String plainText = String.format(
                "You've been invited to join %s on PMT-SK by %s.\n\n" +
                "Role: %s\n\n" +
                "Accept your invitation here: %s\n\n" +
                "This invitation expires on: %s\n\n" +
                "If you weren't expecting this invitation, please ignore this email.",
                workspaceName, inviterName, roleName, inviteUrl, expirationDate
            );

            emailService.sendSimpleEmailWithFallback(
                invitation.getEmail(),
                "You're invited to join " + workspaceName + " on PMT-SK",
                htmlContent,
                plainText
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send workspace invitation email", e);
        }
    }

    private WorkspaceInvitationResponse toResponse(WorkspaceInvitation invitation) {
        return WorkspaceInvitationResponse.builder()
                .id(invitation.getId())
                .workspaceId(invitation.getWorkspace().getId())
                .workspaceName(invitation.getWorkspace().getName())
                .email(invitation.getEmail())
                .invitedByName(invitation.getInvitedBy().getName())
                .workspaceRole(invitation.getWorkspaceRole())
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiresAt())
                .acceptedAt(invitation.getAcceptedAt())
                .createdAt(invitation.getCreatedAt())
                .build();
    }
}



