package com.vinncorp.erp.modules.finance.service;

import com.vinncorp.erp.modules.finance.dto.request.InvoiceCreateRequest;
import com.vinncorp.erp.modules.finance.dto.request.InvoiceUpdateRequest;
import com.vinncorp.erp.modules.finance.dto.response.InvoiceResponse;
import com.vinncorp.erp.modules.finance.dto.response.InvoiceSummaryResponse;
import com.vinncorp.erp.modules.finance.enums.InvoiceStatus;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface InvoiceService {

    InvoiceResponse create(InvoiceCreateRequest request, String actorEmail);

    InvoiceResponse update(Long id, InvoiceUpdateRequest request, String actorEmail);

    void delete(Long id, String actorEmail);

    InvoiceResponse getById(Long id);

    PaginatedResponse<InvoiceResponse> getList(Long workspaceId, String search, InvoiceStatus status,
                                                Long customerId, LocalDateTime dateFrom, LocalDateTime dateTo,
                                                Pageable pageable);

    InvoiceResponse sendInvoice(Long id, String actorEmail);

    InvoiceResponse markAsPaid(Long id, String actorEmail);
}
