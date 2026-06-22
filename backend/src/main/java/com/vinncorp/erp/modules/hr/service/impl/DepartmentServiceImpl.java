package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.hr.entity.Department;
import com.vinncorp.erp.modules.hr.repository.DepartmentRepository;
import com.vinncorp.erp.modules.hr.dto.request.DepartmentCreateRequest;
import com.vinncorp.erp.modules.hr.service.DepartmentService;
import com.vinncorp.erp.modules.hr.dto.response.DepartmentResponse;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest req, Long workspaceId, String actorEmail) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BadRequestException("name is required");
        }
        if (departmentRepository.existsByNameAndWorkspaceId(req.getName(), workspaceId)) {
            throw new ConflictException("Department name already exists in this workspace");
        }
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found: " + actorEmail));

        Department d = new Department();
        d.setName(req.getName());
        d.setCode(req.getCode());
        d.setDescription(req.getDescription());
        d.setHeadEmployeeId(req.getHeadEmployeeId());
        d.setParentDepartmentId(req.getParentDepartmentId());
        d.setActive(req.getActive() == null ? true : req.getActive());
        d.setWorkspace(workspace);
        d.setCreatedBy(actor.getId());
        d.setUpdatedBy(actor.getId());

        return DepartmentResponse.from(departmentRepository.save(d));
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentCreateRequest req, Long workspaceId, String actorEmail) {
        Department d = departmentRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found: " + actorEmail));

        if (req.getName() != null && !req.getName().equals(d.getName())) {
            if (departmentRepository.existsByNameAndWorkspaceId(req.getName(), workspaceId)) {
                throw new ConflictException("Department name already exists in this workspace");
            }
            d.setName(req.getName());
        }
        if (req.getCode() != null) d.setCode(req.getCode());
        if (req.getDescription() != null) d.setDescription(req.getDescription());
        if (req.getHeadEmployeeId() != null) d.setHeadEmployeeId(req.getHeadEmployeeId());
        if (req.getParentDepartmentId() != null) d.setParentDepartmentId(req.getParentDepartmentId());
        if (req.getActive() != null) d.setActive(req.getActive());

        d.setUpdatedBy(actor.getId());
        return DepartmentResponse.from(departmentRepository.save(d));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse get(Long id, Long workspaceId) {
        Department d = departmentRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        return DepartmentResponse.from(d);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> list(Long workspaceId, boolean activeOnly) {
        List<Department> rows = activeOnly
                ? departmentRepository.findAllByWorkspaceIdAndActiveTrue(workspaceId)
                : departmentRepository.findAllByWorkspaceId(workspaceId);
        return rows.stream().map(DepartmentResponse::from).toList();
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId, String actorEmail) {
        Department d = departmentRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found: " + actorEmail));
        d.softDelete(actor.getId());
        d.setUpdatedBy(actor.getId());
        departmentRepository.save(d);
    }
}


