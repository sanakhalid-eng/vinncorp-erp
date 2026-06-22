package com.vinncorp.erp.modules.finance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceCreateRequest {

    @NotBlank(message = "Invoice number is required")
    @Size(max = 64)
    private String invoiceNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long projectId;

    private Long opportunityId;

    @NotNull(message = "Issue date is required")
    private LocalDateTime issueDate;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Size(max = 5000)
    private String notes;

    @NotEmpty(message = "At least one invoice item is required")
    @Valid
    private List<InvoiceItemRequest> items;

    @Data
    public static class InvoiceItemRequest {

        @NotBlank(message = "Item description is required")
        @Size(max = 500)
        private String description;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;
    }
}
