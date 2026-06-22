package com.vinncorp.erp.modules.finance.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseResponse {

    private Long id;
    private Long workspaceId;
    private String title;
    private String description;
    private String category;
    private BigDecimal amount;
    private LocalDateTime expenseDate;
    private String attachmentUrl;
    private String status;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
