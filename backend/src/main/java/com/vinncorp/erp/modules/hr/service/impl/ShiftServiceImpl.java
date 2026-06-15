package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.modules.hr.entity.HrShift;
import com.vinncorp.erp.modules.hr.repository.HrShiftRepository;
import com.vinncorp.erp.modules.hr.request.ShiftCreateRequest;
import com.vinncorp.erp.modules.hr.response.ShiftResponse;
import com.vinncorp.erp.modules.hr.service.ShiftService;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final HrShiftRepository shiftRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional
    public ShiftResponse create(ShiftCreateRequest request, Long workspaceId) {
        if (shiftRepository.existsByNameAndWorkspaceId(request.getName(), workspaceId)) {
            throw new ConflictException("Shift with this name already exists");
        }

        HrShift shift = new HrShift();
        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setBreakMinutes(request.getBreakMinutes() != null ? request.getBreakMinutes() : 0);
        shift.setGracePeriodMinutes(request.getGracePeriodMinutes() != null ? request.getGracePeriodMinutes() : 0);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        shift.setWorkspace(workspace);

        return ShiftResponse.from(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public ShiftResponse update(Long id, ShiftCreateRequest request, Long workspaceId) {
        HrShift shift = shiftRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));

        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        if (request.getBreakMinutes() != null) shift.setBreakMinutes(request.getBreakMinutes());
        if (request.getGracePeriodMinutes() != null) shift.setGracePeriodMinutes(request.getGracePeriodMinutes());

        return ShiftResponse.from(shiftRepository.save(shift));
    }

    @Override
    public ShiftResponse get(Long id, Long workspaceId) {
        HrShift shift = shiftRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
        return ShiftResponse.from(shift);
    }

    @Override
    public List<ShiftResponse> list(Long workspaceId) {
        return shiftRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId)
                .stream().map(ShiftResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<ShiftResponse> listActive(Long workspaceId) {
        return shiftRepository.findByWorkspaceIdAndIsActiveTrueOrderByCreatedAtDesc(workspaceId)
                .stream().map(ShiftResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId) {
        HrShift shift = shiftRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
        shiftRepository.delete(shift);
    }
}
