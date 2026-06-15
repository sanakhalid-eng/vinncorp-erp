package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.request.ShiftCreateRequest;
import com.vinncorp.erp.modules.hr.response.ShiftResponse;

import java.util.List;

public interface ShiftService {
    ShiftResponse create(ShiftCreateRequest request, Long workspaceId);
    ShiftResponse update(Long id, ShiftCreateRequest request, Long workspaceId);
    ShiftResponse get(Long id, Long workspaceId);
    List<ShiftResponse> list(Long workspaceId);
    List<ShiftResponse> listActive(Long workspaceId);
    void delete(Long id, Long workspaceId);
}
