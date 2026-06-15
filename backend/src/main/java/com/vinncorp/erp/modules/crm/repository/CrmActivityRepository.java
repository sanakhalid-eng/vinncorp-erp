package com.vinncorp.erp.modules.crm.repository;

import com.vinncorp.erp.modules.crm.entity.CrmActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmActivityRepository extends JpaRepository<CrmActivity, Long> {
    List<CrmActivity> findByLeadIdOrderByActivityDateDesc(Long leadId);
    List<CrmActivity> findByCustomerIdOrderByActivityDateDesc(Long customerId);
    List<CrmActivity> findByContactIdOrderByActivityDateDesc(Long contactId);
    List<CrmActivity> findByOpportunityIdOrderByActivityDateDesc(Long opportunityId);
    List<CrmActivity> findByWorkspaceIdOrderByActivityDateDesc(Long workspaceId);
    List<CrmActivity> findTop10ByWorkspaceIdOrderByActivityDateDesc(Long workspaceId);

    long countByCustomerIdAndWorkspaceId(Long customerId, Long workspaceId);
}
