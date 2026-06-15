package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.entity.HrProjectAssignment;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import com.vinncorp.erp.modules.hr.repository.HrProjectAssignmentRepository;
import com.vinncorp.erp.modules.hr.request.ProjectAssignmentRequest;
import com.vinncorp.erp.modules.hr.response.ProjectAssignmentResponse;
import com.vinncorp.erp.modules.hr.service.ProjectAssignmentService;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectAssignmentServiceImpl implements ProjectAssignmentService {

    private final HrProjectAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional
    public ProjectAssignmentResponse assign(ProjectAssignmentRequest req, Long workspaceId, String actorEmail) {
        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + req.getEmployeeId()));

        if (assignmentRepository.existsByEmployeeIdAndProjectIdAndWorkspaceId(
                req.getEmployeeId(), req.getProjectId(), workspaceId)) {
            throw new ConflictException("Employee is already assigned to this project");
        }

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found: " + actorEmail));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        HrProjectAssignment assignment = new HrProjectAssignment();
        assignment.setEmployee(employee);
        assignment.setProjectId(req.getProjectId());
        assignment.setProjectName(req.getProjectName());
        assignment.setRoleInProject(req.getRoleInProject());
        assignment.setStartDate(req.getStartDate());
        assignment.setEndDate(req.getEndDate());
        assignment.setAllocationPercentage(req.getAllocationPercentage());
        assignment.setNotes(req.getNotes());
        assignment.setWorkspace(workspace);
        assignment.setCreatedBy(actor.getId());
        assignment.setUpdatedBy(actor.getId());

        HrProjectAssignment saved = assignmentRepository.save(assignment);
        return ProjectAssignmentResponse.from(saved);
    }

    @Override
    @Transactional
    public ProjectAssignmentResponse update(Long id, ProjectAssignmentRequest req, Long workspaceId, String actorEmail) {
        HrProjectAssignment assignment = assignmentRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + id));

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found: " + actorEmail));

        if (req.getRoleInProject() != null) assignment.setRoleInProject(req.getRoleInProject());
        if (req.getStartDate() != null) assignment.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) assignment.setEndDate(req.getEndDate());
        if (req.getAllocationPercentage() != null) assignment.setAllocationPercentage(req.getAllocationPercentage());
        if (req.getNotes() != null) assignment.setNotes(req.getNotes());
        if (req.getProjectName() != null) assignment.setProjectName(req.getProjectName());

        assignment.setUpdatedBy(actor.getId());
        HrProjectAssignment saved = assignmentRepository.save(assignment);
        return ProjectAssignmentResponse.from(saved);
    }

    @Override
    @Transactional
    public ProjectAssignmentResponse unassign(Long id, Long workspaceId, String actorEmail) {
        HrProjectAssignment assignment = assignmentRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + id));

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found: " + actorEmail));

        assignment.setStatus("INACTIVE");
        assignment.setUpdatedBy(actor.getId());
        HrProjectAssignment saved = assignmentRepository.save(assignment);
        return ProjectAssignmentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectAssignmentResponse get(Long id, Long workspaceId) {
        HrProjectAssignment assignment = assignmentRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + id));
        return ProjectAssignmentResponse.from(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAssignmentResponse> getByEmployee(Long employeeId, Long workspaceId) {
        return assignmentRepository.findByEmployeeIdAndWorkspaceIdOrderByStartDateDesc(employeeId, workspaceId)
                .stream().map(ProjectAssignmentResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAssignmentResponse> getByProject(Long projectId, Long workspaceId) {
        return assignmentRepository.findByProjectIdAndWorkspaceIdOrderByStartDateDesc(projectId, workspaceId)
                .stream().map(ProjectAssignmentResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAssignmentResponse> getAll(Long workspaceId) {
        return assignmentRepository.findByWorkspaceIdOrderByStartDateDesc(workspaceId)
                .stream().map(ProjectAssignmentResponse::from).toList();
    }
}
