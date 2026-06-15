package com.vinncorp.erp.modules.crm.service.impl;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.audit.service.AuditService;
import com.vinncorp.erp.modules.crm.entity.CrmActivity;
import com.vinncorp.erp.modules.crm.repository.CrmActivityRepository;
import com.vinncorp.erp.modules.crm.service.CrmActivityService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmActivityServiceImpl implements CrmActivityService {

    private final CrmActivityRepository crmActivityRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public CrmActivity create(CrmActivity activity, Long workspaceId, String actorEmail) {
        if (activity.getSubject() == null || activity.getSubject().isBlank()) {
            throw new BadRequestException("Activity subject is required");
        }
        if (activity.getType() == null) {
            throw new BadRequestException("Activity type is required");
        }
        boolean hasEntity = activity.getContactId() != null || activity.getCustomerId() != null
                || activity.getLeadId() != null || activity.getOpportunityId() != null;
        if (!hasEntity) {
            throw new BadRequestException("Activity must be linked to at least one entity (contact, customer, lead, or opportunity)");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        activity.setWorkspace(workspace);
        activity.setCreatedBy(actor.getId());
        activity.setUpdatedBy(actor.getId());
        return crmActivityRepository.save(activity);
    }

    @Override
    @Transactional
    public CrmActivity update(Long id, CrmActivity updated, Long workspaceId, String actorEmail) {
        CrmActivity existing = crmActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (updated.getSubject() != null) existing.setSubject(updated.getSubject());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getActivityDate() != null) existing.setActivityDate(updated.getActivityDate());
        if (updated.getDurationMinutes() != null) existing.setDurationMinutes(updated.getDurationMinutes());
        existing.setUpdatedBy(actor.getId());
        return crmActivityRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public CrmActivity get(Long id, Long workspaceId) {
        CrmActivity activity = crmActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + id));
        if (!activity.getWorkspaceId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Activity not found in this workspace");
        }
        return activity;
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId, String actorEmail) {
        CrmActivity activity = get(id, workspaceId);
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        activity.softDelete(actor.getId());
        activity.setUpdatedBy(actor.getId());
        crmActivityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmActivity> listByLead(Long leadId) {
        return crmActivityRepository.findByLeadIdOrderByActivityDateDesc(leadId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmActivity> listByCustomer(Long customerId) {
        return crmActivityRepository.findByCustomerIdOrderByActivityDateDesc(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmActivity> listByContact(Long contactId) {
        return crmActivityRepository.findByContactIdOrderByActivityDateDesc(contactId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmActivity> listByOpportunity(Long opportunityId) {
        return crmActivityRepository.findByOpportunityIdOrderByActivityDateDesc(opportunityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmActivity> listRecent(Long workspaceId) {
        return crmActivityRepository.findTop10ByWorkspaceIdOrderByActivityDateDesc(workspaceId);
    }
}
