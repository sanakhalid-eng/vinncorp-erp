package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.dto.request.LeaveTypeCreateRequest;
import com.vinncorp.erp.modules.hr.dto.response.LeaveTypeResponse;

import java.util.List;

public interface LeaveTypeService {
    LeaveTypeResponse create(LeaveTypeCreateRequest request, Long workspaceId);
    LeaveTypeResponse update(Long id, LeaveTypeCreateRequest request, Long workspaceId);
    LeaveTypeResponse get(Long id, Long workspaceId);
    List<LeaveTypeResponse> list(Long workspaceId);
    List<LeaveTypeResponse> listActive(Long workspaceId);
    void delete(Long id, Long workspaceId);
}
