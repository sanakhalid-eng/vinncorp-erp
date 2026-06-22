package com.vinncorp.erp.modules.workflow.service.impl;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowTransitionRequest;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowTransitionResponse;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
import com.vinncorp.erp.modules.workflow.entity.WorkflowTransition;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.workflow.repository.WorkflowStatusRepository;
import com.vinncorp.erp.modules.workflow.repository.WorkflowTransitionRepository;
import com.vinncorp.erp.modules.workflow.service.WorkflowTransitionService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor 
public class WorkflowTransitionServiceImpl implements WorkflowTransitionService {
private final WorkflowTransitionRepository transitionRepository;
private final WorkflowStatusRepository statusRepository;
private final ProjectRepository projectRepository;
@Override 
public WorkflowTransitionResponse createTransition(Long projectId, WorkflowTransitionRequest request) {
Project project = projectRepository.findById(projectId) .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
WorkflowStatus from = statusRepository.findById(request.getFromStatusId()) .orElseThrow(() -> new ResourceNotFoundException("From status not found"));
WorkflowStatus to = statusRepository.findById(request.getToStatusId()) .orElseThrow(() -> new ResourceNotFoundException("To status not found"));
validateTransition(projectId, from, to);
WorkflowTransition transition = new WorkflowTransition();
transition.setFromStatus(from);
transition.setToStatus(to);
transition.setProject(project);
WorkflowTransition saved = transitionRepository.save(transition);
WorkflowTransitionResponse res = new WorkflowTransitionResponse();
res.setId(saved.getId());
res.setFromStatusId(from.getId());
res.setToStatusId(to.getId());
return res;
} @Override 
public List<WorkflowTransitionResponse> getTransitions(Long projectId) {
return transitionRepository.findByProjectId(projectId) .stream() .map(t -> {
WorkflowTransitionResponse r = new WorkflowTransitionResponse();
r.setId(t.getId());
r.setFromStatusId(t.getFromStatus().getId());
r.setToStatusId(t.getToStatus().getId());
return r;
}) .toList();
} @Override 
public void deleteTransition(Long projectId, Long transitionId) {
WorkflowTransition transition = transitionRepository.findById(transitionId) .orElseThrow(() -> new ResourceNotFoundException("Transition not found"));
Long transitionProjectId = transition.getProject() == null ? null : transition.getProject().getId();
if (transitionProjectId == null || !transitionProjectId.equals(projectId)) {
throw new BadRequestException("Transition does not belong to this project");
} transitionRepository.delete(transition);
} 
public void validateTransition(Long projectId, Long fromId, Long toId) {
if (fromId.equals(toId)) {
throw new BadRequestException("Cannot transition to same status");
} WorkflowStatus from = statusRepository.findById(fromId) .orElseThrow(() -> new ResourceNotFoundException("Invalid from status"));
WorkflowStatus to = statusRepository.findById(toId) .orElseThrow(() -> new ResourceNotFoundException("Invalid to status"));
validateTransition(projectId, from, to);
} private void validateTransition(Long projectId, WorkflowStatus from, WorkflowStatus to) {
Long fromProjectId = from.getProject() == null ? null : from.getProject().getId();
Long toProjectId = to.getProject() == null ? null : to.getProject().getId();
if (!projectId.equals(fromProjectId) || !projectId.equals(toProjectId)) {
throw new BadRequestException("Transition statuses must belong to this project");
} if (from.getOrderIndex() >= to.getOrderIndex()) {
throw new BadRequestException("Backflow not allowed");
} boolean exists = transitionRepository .existsByProjectIdAndFromStatusIdAndToStatusId(projectId, from.getId(), to.getId());
if (exists) {
throw new BadRequestException("Transition already exists");
}}} 