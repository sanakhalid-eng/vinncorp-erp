package com.vinncorp.erp.modules.crm.repository;

import com.vinncorp.erp.modules.crm.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByIdAndWorkspaceId(Long id, Long workspaceId);
    List<Contact> findAllByWorkspaceId(Long workspaceId);
    List<Contact> findByWorkspaceIdAndLastNameContainingIgnoreCaseOrWorkspaceIdAndFirstNameContainingIgnoreCase(
            Long workspaceId, String lastName, Long workspaceId2, String firstName);
    boolean existsByEmailAndWorkspaceId(String email, Long workspaceId);
}
