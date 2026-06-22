package com.vinncorp.erp.modules.crm.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.audit.service.AuditService;
import com.vinncorp.erp.modules.crm.entity.Contact;
import com.vinncorp.erp.modules.crm.repository.ContactRepository;
import com.vinncorp.erp.modules.crm.service.ContactService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public Contact create(Contact contact, Long workspaceId, String actorEmail) {
        if (contact.getFirstName() == null || contact.getFirstName().isBlank()) {
            throw new BadRequestException("First name is required");
        }
        if (contact.getLastName() == null || contact.getLastName().isBlank()) {
            throw new BadRequestException("Last name is required");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        contact.setWorkspace(workspace);
        contact.setCreatedBy(actor.getId());
        contact.setUpdatedBy(actor.getId());
        Contact saved = contactRepository.save(contact);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "CREATED", "CONTACT", saved.getId(), saved.getFullName(),
                null, Map.of("firstName", saved.getFirstName(), "lastName", saved.getLastName(), "email", saved.getEmail()),
                null, null);
        return saved;
    }

    @Override
    @Transactional
    public Contact update(Long id, Contact updated, Long workspaceId, String actorEmail) {
        Contact existing = contactRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getCompany() != null) existing.setCompany(updated.getCompany());
        if (updated.getJobTitle() != null) existing.setJobTitle(updated.getJobTitle());
        if (updated.getNotes() != null) existing.setNotes(updated.getNotes());
        existing.setUpdatedBy(actor.getId());
        Contact saved = contactRepository.save(existing);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "UPDATED", "CONTACT", saved.getId(), saved.getFullName(), null, null, null, null);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Contact get(Long id, Long workspaceId) {
        return contactRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> list(Long workspaceId) {
        return contactRepository.findAllByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId, String actorEmail) {
        Contact contact = contactRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        contact.softDelete(actor.getId());
        contact.setUpdatedBy(actor.getId());
        contactRepository.save(contact);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "DELETED", "CONTACT", contact.getId(), contact.getFullName(), null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> search(Long workspaceId, String query) {
        if (query == null || query.isBlank()) return list(workspaceId);
        return contactRepository.findByWorkspaceIdAndLastNameContainingIgnoreCaseOrWorkspaceIdAndFirstNameContainingIgnoreCase(
                workspaceId, query, workspaceId, query);
    }
}
