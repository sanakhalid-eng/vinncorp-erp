package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.hr.entity.HrLeaveType;
import com.vinncorp.erp.modules.hr.repository.HrLeaveTypeRepository;
import com.vinncorp.erp.modules.hr.request.LeaveTypeCreateRequest;
import com.vinncorp.erp.modules.hr.response.LeaveTypeResponse;
import com.vinncorp.erp.modules.hr.service.LeaveTypeService;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final HrLeaveTypeRepository leaveTypeRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional
    public LeaveTypeResponse create(LeaveTypeCreateRequest request, Long workspaceId) {
        if (leaveTypeRepository.existsByNameAndWorkspaceId(request.getName(), workspaceId)) {
            throw new ConflictException("Leave type with this name already exists");
        }
        if (leaveTypeRepository.existsByCodeAndWorkspaceId(request.getCode(), workspaceId)) {
            throw new ConflictException("Leave type with this code already exists");
        }

        HrLeaveType leaveType = new HrLeaveType();
        leaveType.setName(request.getName());
        leaveType.setCode(request.getCode());
        leaveType.setDescription(request.getDescription());
        leaveType.setDefaultDays(request.getDefaultDays());
        leaveType.setIsPaid(request.getIsPaid() != null ? request.getIsPaid() : true);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        leaveType.setWorkspace(workspace);

        return LeaveTypeResponse.from(leaveTypeRepository.save(leaveType));
    }

    @Override
    @Transactional
    public LeaveTypeResponse update(Long id, LeaveTypeCreateRequest request, Long workspaceId) {
        HrLeaveType leaveType = leaveTypeRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        leaveType.setName(request.getName());
        leaveType.setCode(request.getCode());
        leaveType.setDescription(request.getDescription());
        leaveType.setDefaultDays(request.getDefaultDays());
        if (request.getIsPaid() != null) leaveType.setIsPaid(request.getIsPaid());

        return LeaveTypeResponse.from(leaveTypeRepository.save(leaveType));
    }

    @Override
    public LeaveTypeResponse get(Long id, Long workspaceId) {
        HrLeaveType leaveType = leaveTypeRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));
        return LeaveTypeResponse.from(leaveType);
    }

    @Override
    public List<LeaveTypeResponse> list(Long workspaceId) {
        return leaveTypeRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId)
                .stream().map(LeaveTypeResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<LeaveTypeResponse> listActive(Long workspaceId) {
        return leaveTypeRepository.findByWorkspaceIdAndIsActiveTrueOrderByCreatedAtDesc(workspaceId)
                .stream().map(LeaveTypeResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId) {
        HrLeaveType leaveType = leaveTypeRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));
        leaveTypeRepository.delete(leaveType);
    }
}
