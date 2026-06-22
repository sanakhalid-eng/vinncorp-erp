package com.vinncorp.erp.modules.finance.service;

import com.vinncorp.erp.modules.finance.dto.request.PaymentCreateRequest;
import com.vinncorp.erp.modules.finance.dto.request.PaymentUpdateRequest;
import com.vinncorp.erp.modules.finance.dto.response.PaymentResponse;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PaymentService {

    PaymentResponse create(PaymentCreateRequest request, String actorEmail);

    PaymentResponse update(Long id, PaymentUpdateRequest request, String actorEmail);

    void delete(Long id, String actorEmail);

    PaymentResponse getById(Long id);

    PaginatedResponse<PaymentResponse> getList(Long workspaceId, Pageable pageable);

    List<PaymentResponse> getByInvoiceId(Long invoiceId);
}
