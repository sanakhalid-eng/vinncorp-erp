package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.core.workspace.repository.WorkspaceMemberRepository;

import com.vinncorp.erp.core.workspace.enums.InvitationStatus;
import com.vinncorp.erp.modules.projects.dto.request.CreateInvitationRequest;
import com.vinncorp.erp.modules.projects.dto.response.InvitationResponse;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.ProjectInvitation;
import com.vinncorp.erp.modules.projects.entity.ProjectMember;
import com.vinncorp.erp.modules.projects.entity.ProjectRole;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.repository.ProjectInvitationRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRoleRepository;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.CustomAccessDeniedException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final ProjectInvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @Transactional
    public InvitationResponse createInvitation(Long projectId, CreateInvitationRequest request, Long invitedByUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User invitedBy = userRepository.findById(invitedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProjectRole role = projectRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        String email = request.getEmail().trim().toLowerCase();

        // Check if user is already a member via userId
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (projectMemberRepository.existsByProject_IdAndUser_Id(projectId, existingUser.getId())) {
                throw new BadRequestException("User is already a member of this project");
            }
        });

        // Prevent duplicate pending invitations for same email+project+role
        List<ProjectInvitation> existingPending = invitationRepository
                .findByProjectIdAndEmailOrderByCreatedAtDesc(projectId, email).stream()
                .filter(i -> i.getStatus() == InvitationStatus.PENDING)
                .toList();
        boolean sameRolePending = existingPending.stream()
                .anyMatch(i -> i.getRole() != null && i.getRole().getId().equals(request.getRoleId()));
        if (sameRolePending) {
            throw new BadRequestException("A pending invitation already exists for this email with the same role");
        }
        if (!existingPending.isEmpty()) {
            throw new BadRequestException("A pending invitation already exists for this email");
        }

        // Generate raw token and store its SHA-256 hash
        String rawToken = UUID.randomUUID().toString();
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(project);
        invitation.setEmail(email);
        invitation.setInvitedBy(invitedBy);
        invitation.setRole(role);
        invitation.setToken(hashToken(rawToken));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));

        invitation = invitationRepository.save(invitation);

        // Send invitation email with the raw (unhashed) token in the URL
        sendInvitationEmail(invitation, rawToken, invitedBy.getName(), project.getName(), role.getName());

        // Audit log
        activityLogService.logActivity(
                invitedByUserId,
                EntityType.INVITATION,
                invitation.getId(),
                ActionType.INVITATION_CREATED,
                null,
                Map.of("email", email, "role", role.getName(), "projectId", projectId),
                "Invitation sent to " + email + " for project " + project.getName(),
                projectId
        );

        return toResponse(invitation);
    }

    @Transactional
    public InvitationResponse acceptInvitation(String token, Long currentUserId) {
        // Find all pending invitations and use constant-time comparison
        List<ProjectInvitation> allPending = invitationRepository.findByStatus(InvitationStatus.PENDING);
        ProjectInvitation invitation = allPending.stream()
                .filter(i -> constantTimeEquals(hashToken(token), i.getToken()))
                .findFirst()
                .orElse(null);

        if (invitation == null) {
            // Check if token exists at all (for better error message)
            if (invitationRepository.findByToken(hashToken(token)).isPresent()) {
                throw new BadRequestException("Invitation is no longer pending");
            }
            throw new ResourceNotFoundException("Invalid or expired invitation token");
        }

        // Strict expiry check
        if (invitation.isExpired()) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BadRequestException("Invitation has expired");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify email matches
        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            activityLogService.logActivity(
                    currentUserId, EntityType.INVITATION, invitation.getId(),
                    ActionType.SECURITY_VALIDATION_FAILED,
                    Map.of("expectedEmail", invitation.getEmail(), "attemptedEmail", user.getEmail()),
                    null, "Email mismatch on invitation acceptance", invitation.getProject().getId()
            );
            throw new CustomAccessDeniedException("This invitation was sent to a different email address");
        }

        // Auto-join workspace if not already a member
        Long workspaceId = invitation.getProject().getWorkspace().getId();
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, currentUserId)) {
            WorkspaceMember wsMember = new WorkspaceMember();
            wsMember.setWorkspace(invitation.getProject().getWorkspace());
            wsMember.setUser(user);
            wsMember.setWorkspaceRole("WORKSPACE_MEMBER");
            wsMember.setJoinedAt(LocalDateTime.now());
            wsMember.setActive(true);
            workspaceMemberRepository.save(wsMember);
            log.info("Auto-joined user {} to workspace {} after project invitation acceptance",
                    user.getId(), workspaceId);
        }

        // Check if already a member
        if (projectMemberRepository.existsByProject_IdAndUser_Id(invitation.getProject().getId(), currentUserId)) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(LocalDateTime.now());
            invitationRepository.save(invitation);
            throw new BadRequestException("You are already a member of this project");
        }

        // Add user to project
        ProjectMember member = new ProjectMember();
        member.setProject(invitation.getProject());
        member.setUser(user);
        member.setProjectRole(invitation.getRole());
        member.setIsActive(true);
        projectMemberRepository.save(member);

        // Update invitation
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Invalidate all other pending invitations for the same email+project
        List<ProjectInvitation> otherPending = invitationRepository
                .findByProjectIdAndEmailOrderByCreatedAtDesc(invitation.getProject().getId(), invitation.getEmail())
                .stream()
                .filter(i -> i.getStatus() == InvitationStatus.PENDING && !i.getId().equals(invitation.getId()))
                .toList();
        for (ProjectInvitation other : otherPending) {
            other.setStatus(InvitationStatus.REVOKED);
            invitationRepository.save(other);
        }
        if (!otherPending.isEmpty()) {
            log.info("Invalidated {} duplicate pending invitation(s) after acceptance", otherPending.size());
        }

        // Audit log
        activityLogService.logActivity(
                currentUserId,
                EntityType.INVITATION,
                invitation.getId(),
                ActionType.INVITATION_ACCEPTED,
                null,
                Map.of("email", user.getEmail(), "role", invitation.getRole().getName(), "projectId", invitation.getProject().getId()),
                "User " + user.getName() + " accepted invitation to project " + invitation.getProject().getName(),
                invitation.getProject().getId()
        );

        return toResponse(invitation);
    }

    @Transactional
    public void revokeInvitation(Long invitationId, Long currentUserId) {
        ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Can only revoke pending invitations");
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepository.save(invitation);

        activityLogService.logActivity(
                currentUserId,
                EntityType.INVITATION,
                invitation.getId(),
                ActionType.INVITATION_REVOKED,
                Map.of("email", invitation.getEmail(), "status", invitation.getStatus().name()),
                null,
                "Invitation revoked for " + invitation.getEmail(),
                invitation.getProject().getId()
        );
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getProjectInvitations(Long projectId) {
        return invitationRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public InvitationResponse getInvitationByToken(String token) {
        String hashedToken = hashToken(token);
        List<ProjectInvitation> candidates = invitationRepository.findByStatus(InvitationStatus.PENDING);
        ProjectInvitation invitation = candidates.stream()
                .filter(i -> constantTimeEquals(hashedToken, i.getToken()))
                .findFirst()
                .orElse(null);

        if (invitation == null) {
            invitation = invitationRepository.findByToken(hashedToken)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token"));
        }

        if (invitation.isExpired() && invitation.getStatus() == InvitationStatus.PENDING) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
        }

        return toResponse(invitation);
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

    private void sendInvitationEmail(ProjectInvitation invitation, String rawToken, String inviterName, String projectName, String roleName) {
        try {
            String inviteUrl = baseUrl + "/invite/" + rawToken;
            String expirationDate = invitation.getExpiresAt().toLocalDate().toString();

            Map<String, Object> variables = new HashMap<>();
            variables.put("inviterName", inviterName);
            variables.put("projectName", projectName);
            variables.put("roleName", roleName);
            variables.put("inviteUrl", inviteUrl);
            variables.put("expirationDate", expirationDate);

            String htmlContent = emailTemplateService.loadTemplate("invitation.html", variables);

            // Plaintext fallback
            String plainText = String.format(
                    """
                            You've been invited to join %s on PMT-SK by %s.
                            
                            Role: %s
                            
                            Accept your invitation here: %s
                            
                            This invitation expires on: %s
                            
                            If you weren't expecting this invitation, please ignore this email.""",
                projectName, inviterName, roleName, inviteUrl, expirationDate
            );

            emailService.sendSimpleEmailWithFallback(
                invitation.getEmail(),
                "You're invited to join " + projectName + " on PMT-SK",
                htmlContent,
                plainText
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send invitation email", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void expireStaleInvitations() {
        List<ProjectInvitation> expired = invitationRepository.findExpiredPendingInvitations();
        for (ProjectInvitation invitation : expired) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            log.info("Expired invitation {} for {}", invitation.getId(), invitation.getEmail());
        }
        if (!expired.isEmpty()) {
            log.info("Expired {} stale invitation(s)", expired.size());
        }
    }

    private InvitationResponse toResponse(ProjectInvitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .projectId(invitation.getProject().getId())
                .projectName(invitation.getProject().getName())
                .email(invitation.getEmail())
                .invitedByName(invitation.getInvitedBy().getName())
                .roleName(invitation.getRole() != null ? invitation.getRole().getName() : null)
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiresAt())
                .acceptedAt(invitation.getAcceptedAt())
                .createdAt(invitation.getCreatedAt())
                .build();
    }
}



