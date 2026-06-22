package com.vinncorp.erp.modules.finance.service.impl;

import com.vinncorp.erp.modules.finance.dto.response.FinanceDashboardResponse;
import com.vinncorp.erp.modules.finance.repository.ExpenseRepository;
import com.vinncorp.erp.modules.finance.repository.InvoiceRepository;
import com.vinncorp.erp.modules.finance.service.FinanceDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FinanceDashboardServiceImpl implements FinanceDashboardService {

    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional(readOnly = true)
    public FinanceDashboardResponse getDashboard(Long workspaceId) {
        long outstandingInvoices = invoiceRepository.countOutstandingByWorkspaceId(workspaceId);
        long overdueInvoices = invoiceRepository.countOverdueByWorkspaceId(workspaceId);
        BigDecimal totalRevenue = invoiceRepository.totalRevenueByWorkspaceId(workspaceId);
        BigDecimal expenses = expenseRepository.totalExpensesByWorkspaceId(workspaceId);
        BigDecimal profit = totalRevenue.subtract(expenses);

        return FinanceDashboardResponse.builder()
                .outstandingInvoices(outstandingInvoices)
                .overdueInvoices(overdueInvoices)
                .totalRevenue(totalRevenue)
                .expenses(expenses)
                .profit(profit)
                .build();
    }
}
