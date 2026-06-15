package com.vinncorp.erp.modules.crm.service;

import com.vinncorp.erp.modules.crm.entity.Lead;
import com.vinncorp.erp.modules.crm.enums.LeadStatus;
import java.util.List;

public interface LeadService {
    Lead create(Lead lead, Long workspaceId, String actorEmail);
    Lead update(Long id, Lead lead, Long workspaceId, String actorEmail);
    Lead get(Long id, Long workspaceId);
    List<Lead> list(Long workspaceId);
    List<Lead> listByStatus(Long workspaceId, LeadStatus status);
    void delete(Long id, Long workspaceId, String actorEmail);
    Lead convert(Long id, Long workspaceId, String actorEmail);
    long countByStatus(Long workspaceId, LeadStatus status);
    List<Lead> search(Long workspaceId, String query);
}
