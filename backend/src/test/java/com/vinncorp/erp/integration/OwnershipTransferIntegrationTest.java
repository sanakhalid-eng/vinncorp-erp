package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;
import com.vinncorp.erp.core.workspace.request.TransferOwnershipRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.CustomAccessDeniedException;
import com.vinncorp.erp.shared.security.MembershipResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OwnershipTransferIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MembershipResolver membershipResolver;

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email, "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
    }

    @Test
    void transferOwnership_fromAdminToAdmin_shouldSucceed() {
        authenticateAs(adminUser.getEmail());

        TransferOwnershipRequest request = new TransferOwnershipRequest();
        request.setTargetUserId(secondAdmin.getId());

        ResponseEntity<ApiResponse<Void>> responseEntity = systemController.transferOwnership(request,
                SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(responseEntity);
        ApiResponse<Void> response = responseEntity.getBody();

        assert response != null;
        assertTrue(response.isSuccess());

        assertFalse(membershipResolver.isWorkspaceOwner(adminUser.getEmail()));
        assertTrue(membershipResolver.isWorkspaceOwner(secondAdmin.getEmail()));
    }

    @Test
    void transferOwnership_byNonOwner_shouldFail() {
        authenticateAs(normalUser.getEmail());

        TransferOwnershipRequest request = new TransferOwnershipRequest();
        request.setTargetUserId(secondAdmin.getId());

        assertThrows(CustomAccessDeniedException.class, () ->
                systemController.transferOwnership(request,
                        SecurityContextHolder.getContext().getAuthentication()));
    }

    @Test
    void transferOwnership_toNonAdmin_shouldFail() {
        authenticateAs(adminUser.getEmail());

        TransferOwnershipRequest request = new TransferOwnershipRequest();
        request.setTargetUserId(normalUser.getId());

        assertThrows(BadRequestException.class, () ->
                systemController.transferOwnership(request,
                        SecurityContextHolder.getContext().getAuthentication()));
    }

    @Test
    void transferOwnership_toSelf_shouldFail() {
        authenticateAs(adminUser.getEmail());

        TransferOwnershipRequest request = new TransferOwnershipRequest();
        request.setTargetUserId(adminUser.getId());

        assertThrows(BadRequestException.class, () ->
                systemController.transferOwnership(request,
                        SecurityContextHolder.getContext().getAuthentication()));
    }

    @Test
    void newOwnerCanTransfer_toAnotherAdmin() {
        authenticateAs(adminUser.getEmail());
        TransferOwnershipRequest request1 = new TransferOwnershipRequest();
        request1.setTargetUserId(secondAdmin.getId());
        systemController.transferOwnership(request1,
                SecurityContextHolder.getContext().getAuthentication());

        assertTrue(membershipResolver.isWorkspaceOwner(secondAdmin.getEmail()));
        assertFalse(membershipResolver.isWorkspaceOwner(adminUser.getEmail()));

        authenticateAs(secondAdmin.getEmail());
        TransferOwnershipRequest request2 = new TransferOwnershipRequest();
        request2.setTargetUserId(adminUser.getId());
        ResponseEntity<ApiResponse<Void>> responseEntity2 = systemController.transferOwnership(request2,
                SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(responseEntity2);
        ApiResponse<Void> response2 = responseEntity2.getBody();

        assertTrue(response2.isSuccess());
        assertTrue(membershipResolver.isWorkspaceOwner(adminUser.getEmail()));
        assertFalse(membershipResolver.isWorkspaceOwner(secondAdmin.getEmail()));
    }

    @Test
    void membershipResolver_correctlyIdentifiesOwner() {
        assertTrue(membershipResolver.isWorkspaceOwner(adminUser));
        assertFalse(membershipResolver.isWorkspaceOwner(normalUser));
        assertFalse(membershipResolver.isWorkspaceOwner(secondAdmin));
        assertEquals(adminUser.getId(), membershipResolver.resolveWorkspaceOwnerId());
    }
}

