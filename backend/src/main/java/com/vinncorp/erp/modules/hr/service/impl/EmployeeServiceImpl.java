package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.enums.EmployeeStatus;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import com.vinncorp.erp.modules.hr.service.EmployeeService;
import com.vinncorp.erp.modules.hr.request.EmployeeCreateRequest;
import com.vinncorp.erp.modules.hr.request.EmployeeUpdateRequest;
import com.vinncorp.erp.modules.hr.response.EmployeeResponse;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest req, Long workspaceId, String actorEmail) {
        if (req.getHireDate() == null) {
            throw new BadRequestException("hireDate is required");
        }
        if (req.getEmployeeCode() == null || req.getEmployeeCode().isBlank()) {
            throw new BadRequestException("employeeCode is required");
        }

        if (employeeRepository.existsByEmployeeCodeAndWorkspaceId(req.getEmployeeCode(), workspaceId)) {
            throw new ConflictException("Employee code already exists in this workspace");
        }

        if (req.getUserId() != null
                && employeeRepository.existsByUserIdAndWorkspaceId(req.getUserId(), workspaceId)) {
            throw new ConflictException("This user is already linked to an employee in this workspace");
        }

        if (req.getUserId() != null) {
            userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Linked user not found: " + req.getUserId()));
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found: " + actorEmail));

        Employee e = new Employee();
        e.setEmployeeCode(req.getEmployeeCode());
        e.setFirstName(req.getFirstName());
        e.setLastName(req.getLastName());
        e.setWorkEmail(req.getWorkEmail());
        e.setPersonalEmail(req.getPersonalEmail());
        e.setPhone(req.getPhone());
        e.setEmploymentType(req.getEmploymentType() != null
                ? req.getEmploymentType()
                : com.vinncorp.erp.modules.hr.enums.EmploymentType.FULL_TIME);
        e.setStatus(req.getStatus() != null ? req.getStatus() : EmployeeStatus.ACTIVE);
        e.setDateOfBirth(req.getDateOfBirth());
        e.setHireDate(req.getHireDate());
        e.setTerminationDate(req.getTerminationDate());
        e.setJobTitle(req.getJobTitle());
        e.setTimezone(req.getTimezone());
        e.setLocale(req.getLocale());
        e.setManagerId(req.getManagerId());
        e.setUserId(req.getUserId());
        e.setDepartmentId(req.getDepartmentId());
        e.setDesignationId(req.getDesignationId());
        e.setWorkspace(workspace);
        e.setCreatedBy(actor.getId());
        e.setUpdatedBy(actor.getId());

        Employee saved = employeeRepository.save(e);
        return EmployeeResponse.from(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest req, Long workspaceId, String actorEmail) {
        Employee e = employeeRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found: " + actorEmail));

        if (req.getFirstName() != null) e.setFirstName(req.getFirstName());
        if (req.getLastName() != null) e.setLastName(req.getLastName());
        if (req.getWorkEmail() != null) e.setWorkEmail(req.getWorkEmail());
        if (req.getPersonalEmail() != null) e.setPersonalEmail(req.getPersonalEmail());
        if (req.getPhone() != null) e.setPhone(req.getPhone());
        if (req.getEmploymentType() != null) e.setEmploymentType(req.getEmploymentType());
        if (req.getStatus() != null) e.setStatus(req.getStatus());
        if (req.getDateOfBirth() != null) e.setDateOfBirth(req.getDateOfBirth());
        if (req.getHireDate() != null) e.setHireDate(req.getHireDate());
        if (req.getTerminationDate() != null) e.setTerminationDate(req.getTerminationDate());
        if (req.getJobTitle() != null) e.setJobTitle(req.getJobTitle());
        if (req.getTimezone() != null) e.setTimezone(req.getTimezone());
        if (req.getLocale() != null) e.setLocale(req.getLocale());
        if (req.getManagerId() != null) e.setManagerId(req.getManagerId());
        if (req.getUserId() != null) {
            if (!req.getUserId().equals(e.getUserId())
                    && employeeRepository.existsByUserIdAndWorkspaceId(req.getUserId(), workspaceId)) {
                throw new ConflictException("This user is already linked to an employee in this workspace");
            }
            e.setUserId(req.getUserId());
        }
        if (req.getDepartmentId() != null) e.setDepartmentId(req.getDepartmentId());
        if (req.getDesignationId() != null) e.setDesignationId(req.getDesignationId());

        e.setUpdatedBy(actor.getId());
        Employee saved = employeeRepository.save(e);
        return EmployeeResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse get(Long id, Long workspaceId) {
        Employee e = employeeRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        return EmployeeResponse.from(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> list(Long workspaceId, Long departmentId, EmployeeStatus status) {
        List<Employee> rows;
        if (departmentId != null && status != null) {
            rows = employeeRepository.findAllByWorkspaceIdAndDepartmentId(workspaceId, departmentId)
                    .stream().filter(x -> x.getStatus() == status).toList();
        } else if (departmentId != null) {
            rows = employeeRepository.findAllByWorkspaceIdAndDepartmentId(workspaceId, departmentId);
        } else if (status != null) {
            rows = employeeRepository.findAllByWorkspaceIdAndStatus(workspaceId, status);
        } else {
            rows = employeeRepository.findAllByWorkspaceId(workspaceId);
        }
        return rows.stream().map(EmployeeResponse::from).toList();
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId, String actorEmail) {
        Employee e = employeeRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found: " + actorEmail));
        e.softDelete(actor.getId());
        e.setUpdatedBy(actor.getId());
        employeeRepository.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getByUserId(Long userId, Long workspaceId) {
        Employee e = employeeRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user: " + userId));
        return EmployeeResponse.from(e);
    }
}


