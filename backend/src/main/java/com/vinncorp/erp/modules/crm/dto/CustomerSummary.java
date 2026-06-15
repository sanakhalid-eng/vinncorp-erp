package com.vinncorp.erp.modules.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummary {
    private Long customerId;
    private String customerName;
    private BigDecimal totalRevenue;
    private long wonDeals;
    private long openOpportunities;
    private long activeProjects;
    private long closedProjects;
    private long recentActivities;
}
