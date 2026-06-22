package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.dto.request.HolidayCreateRequest;
import com.vinncorp.erp.modules.hr.dto.response.HolidayResponse;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {
    HolidayResponse create(HolidayCreateRequest request, Long workspaceId);
    HolidayResponse update(Long id, HolidayCreateRequest request, Long workspaceId);
    HolidayResponse get(Long id, Long workspaceId);
    List<HolidayResponse> list(Long workspaceId);
    List<HolidayResponse> getByDateRange(Long workspaceId, LocalDate startDate, LocalDate endDate);
    void delete(Long id, Long workspaceId);
}
