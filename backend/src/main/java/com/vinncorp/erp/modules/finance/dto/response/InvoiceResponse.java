package com.vinncorp.erp.modules.finance.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceResponse {

    private Long id;
    private Long workspaceId;
    private String invoiceNumber;
    private Long customerId;
    private Long projectId;
    private Long opportunityId;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private String notes;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InvoiceItemResponse> items;
    private List<PaymentResponse> payments;
}
