package com.vinncorp.erp.modules.crm.service;

import com.vinncorp.erp.modules.crm.entity.CrmActivity;
import java.util.List;

public interface CrmActivityService {
    CrmActivity create(CrmActivity activity, Long workspaceId, String actorEmail);
    CrmActivity update(Long id, CrmActivity activity, Long workspaceId, String actorEmail);
    CrmActivity get(Long id, Long workspaceId);
    void delete(Long id, Long workspaceId, String actorEmail);
    List<CrmActivity> listByLead(Long leadId);
    List<CrmActivity> listByCustomer(Long customerId);
    List<CrmActivity> listByContact(Long contactId);
    List<CrmActivity> listByOpportunity(Long opportunityId);
    List<CrmActivity> listRecent(Long workspaceId);
}
