package com.vinncorp.erp.modules.crm.service;

import com.vinncorp.erp.modules.crm.entity.Contact;
import com.vinncorp.erp.modules.crm.entity.Customer;
import com.vinncorp.erp.modules.crm.entity.Lead;
import com.vinncorp.erp.modules.crm.entity.Opportunity;

import java.util.List;

public interface CrmProjectIntegrationService {

    /**
     * Creates a Project from a Won Opportunity.
     * Opportunity owner becomes Project Manager.
     */
    com.vinncorp.erp.modules.projects.entity.Project createProjectFromOpportunity(
            Opportunity opportunity, Customer customer, String actorEmail);

    /**
     * Resolves workspace ID from the opportunity's workspace.
     */
    Long resolveWorkspaceId(Long workspaceId);
}
