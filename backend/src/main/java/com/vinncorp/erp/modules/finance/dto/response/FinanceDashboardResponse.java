package com.vinncorp.erp.modules.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDashboardResponse {

    private long outstandingInvoices;
    private long overdueInvoices;
    private BigDecimal totalRevenue;
    private BigDecimal expenses;
    private BigDecimal profit;
}
