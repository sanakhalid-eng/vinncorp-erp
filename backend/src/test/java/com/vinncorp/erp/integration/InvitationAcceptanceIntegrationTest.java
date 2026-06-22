package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.workspace.enums.InvitationStatus;
import com.vinncorp.erp.modules.projects.dto.response.InvitationResponse;
import com.vinncorp.erp.modules.projects.entity.ProjectInvitation;
import com.vinncorp.erp.modules.projects.repository.ProjectInvitationRepository;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class InvitationAcceptanceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProjectInvitationRepository invitationRepository;

    private static final String RAW_TOKEN = "test-raw-token-12345";

    private User newUser;

    @BeforeEach
    void setUp() {
        invitationRepository.deleteAll();

        newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("newuser@test.com");
        newUser.setPassword("encoded-pass");
        newUser.setWorkspaceOwner(false);
        newUser.setActive(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser = userRepository.save(newUser);
    }

    @Test
    void acceptInvitation_withKnownToken_shouldSucceed() {
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(testProject);
        invitation.setEmail(newUser.getEmail());
        invitation.setInvitedBy(adminUser);
        invitation.setRole(teamMemberRole);
        invitation.setToken(hashToken(RAW_TOKEN));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitationRepository.save(invitation);

        InvitationResponse accepted = invitationService.acceptInvitation(RAW_TOKEN, newUser.getId());

        assertEquals(InvitationStatus.ACCEPTED.name(), accepted.getStatus());
        boolean isMember = projectMemberRepository
                .existsByProject_IdAndUser_Id(testProject.getId(), newUser.getId());
        assertTrue(isMember);
    }

    @Test
    void acceptInvitation_withInvalidToken_shouldFail() {
        assertThrows(ResourceNotFoundException.class, () ->
                invitationService.acceptInvitation("invalid-token", newUser.getId()));
    }

    @Test
    void inviteAndRevoke_shouldSucceed() {
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(testProject);
        invitation.setEmail(newUser.getEmail());
        invitation.setInvitedBy(adminUser);
        invitation.setRole(teamMemberRole);
        invitation.setToken(hashToken(RAW_TOKEN + "revoke"));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation = invitationRepository.save(invitation);

        invitationService.revokeInvitation(invitation.getId(), adminUser.getId());
        ProjectInvitation revoked = invitationRepository.findById(invitation.getId()).orElseThrow();
        assertEquals(InvitationStatus.REVOKED, revoked.getStatus());
    }

    @Test
    void getProjectInvitations_shouldReturnAll() {
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(testProject);
        invitation.setEmail(newUser.getEmail());
        invitation.setInvitedBy(adminUser);
        invitation.setRole(teamMemberRole);
        invitation.setToken(hashToken(RAW_TOKEN + "list"));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitationRepository.save(invitation);

        var invitations = invitationService.getProjectInvitations(testProject.getId());
        assertEquals(1, invitations.size());
    }

    @Test
    void acceptInvitation_forAlreadyMember_shouldFail() {
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(testProject);
        invitation.setEmail(normalUser.getEmail());
        invitation.setInvitedBy(adminUser);
        invitation.setRole(teamMemberRole);
        invitation.setToken(hashToken(RAW_TOKEN));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitationRepository.save(invitation);

        assertThrows(BadRequestException.class, () ->
                invitationService.acceptInvitation(RAW_TOKEN, normalUser.getId()));
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
            throw new RuntimeException(e);
        }
    }
}

