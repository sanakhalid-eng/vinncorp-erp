package com.vinncorp.erp.modules.crm.service;

import com.vinncorp.erp.modules.crm.entity.Opportunity;
import java.util.List;

public interface OpportunityService {
    Opportunity create(Opportunity opp, Long workspaceId, String actorEmail);
    Opportunity update(Long id, Opportunity opp, Long workspaceId, String actorEmail);
    Opportunity get(Long id, Long workspaceId);
    List<Opportunity> list(Long workspaceId);
    void delete(Long id, Long workspaceId, String actorEmail);
    Opportunity changeStage(Long id, Long stageId, Long workspaceId, String actorEmail);
    Opportunity won(Long id, Long workspaceId, String actorEmail);
    Opportunity lost(Long id, Long workspaceId, String actorEmail);
    List<Opportunity> listByStage(Long workspaceId, Long stageId);
    List<Opportunity> listOpen(Long workspaceId);
}
