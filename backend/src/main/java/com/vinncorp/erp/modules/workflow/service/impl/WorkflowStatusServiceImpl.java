package com.vinncorp.erp.modules.workflow.service.impl;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowStatusOrderRequest;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowStatusRequest;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowStatusResponse;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.workflow.entity.WorkflowStatus;
import com.vinncorp.erp.modules.workflow.mapper.WorkflowStatusMapper;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.workflow.repository.WorkflowStatusRepository;
import com.vinncorp.erp.modules.workflow.service.WorkflowStatusService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor 
public class WorkflowStatusServiceImpl implements WorkflowStatusService {
private final WorkflowStatusRepository workflowStatusRepository;
private final ProjectRepository projectRepository;
private final TaskRepository taskRepository;
@Override 
public WorkflowStatusResponse createStatus(Long projectId, WorkflowStatusRequest request) {
Project project = projectRepository.findById(projectId) .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
WorkflowStatus status = new WorkflowStatus();
status.setName(request.getName());
status.setColor(request.getColor());
status.setOrderIndex(request.getOrderIndex());
status.setDefault(false);
status.setProject(project);
return WorkflowStatusMapper.toResponse(workflowStatusRepository.save(status));
} @Override 
public WorkflowStatusResponse updateStatus(Long projectId, Long statusId, WorkflowStatusRequest request) {
WorkflowStatus status = workflowStatusRepository.findById(statusId) .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
validateStatusProject(projectId, status);
status.setName(request.getName());
status.setColor(request.getColor());
if (request.getOrderIndex() != null) {
status.setOrderIndex(request.getOrderIndex());
} return WorkflowStatusMapper.toResponse(workflowStatusRepository.save(status));
} @Override 
public List<WorkflowStatusResponse> getStatuses(Long projectId) {
return workflowStatusRepository.findByProjectIdOrderByOrderIndexAsc(projectId) .stream() .map(WorkflowStatusMapper::toResponse) .toList();
} @Override 
public List<WorkflowStatusResponse> reorderStatuses(Long projectId, List<WorkflowStatusOrderRequest> request) {
List<WorkflowStatus> statuses = workflowStatusRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
Map<Long, WorkflowStatus> byId = statuses.stream() .collect(Collectors.toMap(WorkflowStatus::getId, Function.identity()));
for (WorkflowStatusOrderRequest item : request) {
WorkflowStatus status = byId.get(item.getId());
if (status == null) {
throw new ResourceNotFoundException("Status not found");
} status.setOrderIndex(item.getOrderIndex());
} workflowStatusRepository.saveAll(statuses);
return workflowStatusRepository.findByProjectIdOrderByOrderIndexAsc(projectId) .stream() .map(WorkflowStatusMapper::toResponse) .toList();
} @Override 
public void deleteStatus(Long statusId) {
WorkflowStatus status = workflowStatusRepository.findById(statusId) .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
if (status.isDefault()) {
throw new BadRequestException("Default status cannot be deleted");
} if (taskRepository.countByStatusEntity_Id(statusId) > 0) {
throw new BadRequestException("This status is currently used by tasks");
} workflowStatusRepository.delete(status);
} private void validateStatusProject(Long projectId, WorkflowStatus status) {
Long statusProjectId = status.getProject() == null ? null : status.getProject().getId();
if (statusProjectId == null || !statusProjectId.equals(projectId)) {
throw new BadRequestException("Status does not belong to this project");
}}} 