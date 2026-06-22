package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.enums.LeaveRequestStatus;
import com.vinncorp.erp.modules.hr.dto.request.LeaveRequestActionRequest;
import com.vinncorp.erp.modules.hr.dto.request.LeaveRequestCreateRequest;
import com.vinncorp.erp.modules.hr.dto.response.LeaveRequestResponse;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequestResponse apply(LeaveRequestCreateRequest request, Long workspaceId, Long currentUserId);
    LeaveRequestResponse approve(Long id, Long workspaceId, Long approvedByUserId);
    LeaveRequestResponse reject(Long id, LeaveRequestActionRequest request, Long workspaceId, Long rejectedByUserId);
    LeaveRequestResponse cancel(Long id, Long workspaceId, Long cancelledByUserId);
    LeaveRequestResponse get(Long id, Long workspaceId);
    List<LeaveRequestResponse> listByEmployee(Long employeeId, Long workspaceId);
    List<LeaveRequestResponse> listByStatus(LeaveRequestStatus status, Long workspaceId);
    List<LeaveRequestResponse> listAll(Long workspaceId);
    long countPending(Long workspaceId);
}
