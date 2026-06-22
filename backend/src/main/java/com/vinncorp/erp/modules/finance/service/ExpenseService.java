package com.vinncorp.erp.modules.finance.service;

import com.vinncorp.erp.modules.finance.dto.request.ExpenseCreateRequest;
import com.vinncorp.erp.modules.finance.dto.request.ExpenseUpdateRequest;
import com.vinncorp.erp.modules.finance.dto.response.ExpenseResponse;
import com.vinncorp.erp.modules.finance.enums.ExpenseStatus;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface ExpenseService {

    ExpenseResponse create(ExpenseCreateRequest request, String actorEmail);

    ExpenseResponse update(Long id, ExpenseUpdateRequest request, String actorEmail);

    void delete(Long id, String actorEmail);

    ExpenseResponse getById(Long id);

    PaginatedResponse<ExpenseResponse> getList(Long workspaceId, String search, ExpenseStatus status,
                                                String category, LocalDateTime dateFrom, LocalDateTime dateTo,
                                                Pageable pageable);

    ExpenseResponse approve(Long id, String actorEmail);

    ExpenseResponse reject(Long id, String actorEmail);

    ExpenseResponse reimburse(Long id, String actorEmail);
}
