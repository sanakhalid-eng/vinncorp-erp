package com.vinncorp.erp.modules.finance.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InvoiceSummaryResponse {

    private Long id;
    private String invoiceNumber;
    private Long customerId;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private String status;
    private LocalDateTime createdAt;
}
