package com.vinncorp.erp.modules.crm.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.crm.entity.CrmActivity;
import com.vinncorp.erp.modules.crm.entity.Customer;
import com.vinncorp.erp.modules.crm.service.CrmActivityService;
import com.vinncorp.erp.modules.crm.entity.Lead;
import com.vinncorp.erp.modules.crm.entity.Opportunity;
import com.vinncorp.erp.modules.crm.enums.LeadStatus;
import com.vinncorp.erp.modules.crm.repository.CustomerRepository;
import com.vinncorp.erp.modules.crm.repository.OpportunityRepository;
import com.vinncorp.erp.modules.crm.service.LeadService;
import com.vinncorp.erp.modules.crm.service.OpportunityService;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crm/dashboard")
@RequiredArgsConstructor
@Tag(name = "CRM Dashboard")
@PreAuthorize("isAuthenticated()")
public class CrmDashboardController {

    private final LeadService leadService;
    private final OpportunityService opportunityService;
    private final CrmActivityService crmActivityService;
    private final CurrentWorkspaceResolver workspaceResolver;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final OpportunityRepository opportunityRepository;
    private final CustomerRepository customerRepository;

    @GetMapping
    @Operation(summary = "CRM dashboard summary")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();

        Map<String, Object> dashboard = new LinkedHashMap<>();

        // Lead counts by status
        Map<String, Long> leadsByStatus = new LinkedHashMap<>();
        for (LeadStatus status : LeadStatus.values()) {
            leadsByStatus.put(status.name(), leadService.countByStatus(wsId, status));
        }
        dashboard.put("leadsByStatus", leadsByStatus);
        dashboard.put("leadsThisMonth", leadsByStatus.getOrDefault("NEW", 0L) + leadsByStatus.getOrDefault("CONTACTED", 0L));

        // Total leads
        long totalLeads = leadsByStatus.values().stream().mapToLong(Long::longValue).sum();
        dashboard.put("totalLeads", totalLeads);

        // Opportunities
        List<Opportunity> openOpps = opportunityService.listOpen(wsId);
        dashboard.put("openOpportunities", openOpps.size());

        // Pipeline value (sum of open opportunities)
        BigDecimal pipelineValue = opportunityRepository.sumPipelineValue(wsId);
        dashboard.put("pipelineValue", pipelineValue != null ? pipelineValue : BigDecimal.ZERO);

        // Won revenue
        BigDecimal wonRevenue = opportunityRepository.sumWonValue(wsId);
        dashboard.put("wonRevenue", wonRevenue != null ? wonRevenue : BigDecimal.ZERO);

        // Won deals count
        long wonDeals = openOpps.isEmpty() ? 0 : 0;
        List<Opportunity> allOpps = opportunityService.list(wsId);
        long wonCount = allOpps.stream()
                .filter(o -> o.getStage() != null && o.getStage().isWon())
                .count();
        dashboard.put("wonDeals", wonCount);

        // Total customers
        List<Customer> allCustomers = customerRepository.findAllByWorkspaceId(wsId);
        dashboard.put("totalCustomers", allCustomers.size());

        // Top customers (by number of linked opportunities)
        List<Map<String, Object>> topCustomers = new ArrayList<>();
        allCustomers.stream()
                .limit(5)
                .forEach(c -> {
                    List<Opportunity> customerOpps = opportunityRepository.findByCustomerIdAndWorkspaceIdOrderByCreatedAtDesc(c.getId(), wsId);
                    BigDecimal totalValue = customerOpps.stream()
                            .map(o -> o.getValue() != null ? o.getValue() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Map<String, Object> customerMap = new LinkedHashMap<>();
                    customerMap.put("id", c.getId());
                    customerMap.put("name", c.getName());
                    customerMap.put("email", c.getEmail());
                    customerMap.put("opportunityCount", customerOpps.size());
                    customerMap.put("totalValue", totalValue);
                    topCustomers.add(customerMap);
                });
        dashboard.put("topCustomers", topCustomers);

        // Recent activities
        List<CrmActivity> recentActivities = crmActivityService.listRecent(wsId);
        dashboard.put("recentActivities", recentActivities);

        // Project count
        long projectCount = projectRepository.countByWorkspaceIdAndDeletedAtIsNull(wsId);
        dashboard.put("totalProjects", projectCount);

        return ResponseEntity.ok(dashboard);
    }
}
