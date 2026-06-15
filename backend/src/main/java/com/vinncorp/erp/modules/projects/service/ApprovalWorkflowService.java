package com.vinncorp.erp.modules.projects.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.websocket.WebSocketEventDispatcher;
import com.vinncorp.erp.modules.projects.entity.ApprovalRequest;
import com.vinncorp.erp.modules.projects.entity.ApprovalStep;
import com.vinncorp.erp.modules.projects.entity.ApprovalWorkflow;
import com.vinncorp.erp.modules.projects.event.WebSocketEvent;
import com.vinncorp.erp.modules.projects.repository.ApprovalRequestRepository;
import com.vinncorp.erp.modules.projects.repository.ApprovalStepRepository;
import com.vinncorp.erp.modules.projects.repository.ApprovalWorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalWorkflowService {

    private final ApprovalWorkflowRepository workflowRepository;
    private final ApprovalRequestRepository requestRepository;
    private final ApprovalStepRepository stepRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final WebSocketEventDispatcher eventDispatcher;

    @Transactional
    public ApprovalWorkflow createWorkflow(Long workspaceId, Long userId, String name, String description,
                                           String entityType, String approvalChain) {
        Workspace workspace = getWorkspace(workspaceId);

        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setWorkspace(workspace);
        workflow.setName(name);
        workflow.setDescription(description);
        workflow.setEntityType(entityType);
        workflow.setApprovalChain(approvalChain);
        workflow.setActive(true);

        ApprovalWorkflow saved = workflowRepository.save(workflow);
        log.info("Created approval workflow: {} for workspace {}", name, workspaceId);
        return saved;
    }

    @Transactional
    public ApprovalRequest submitRequest(Long workflowId, Long userId, String entityType, Long entityId) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found"));

        if (!workflow.isActive()) {
            throw new BadRequestException("Approval workflow is not active");
        }

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ApprovalRequest request = new ApprovalRequest();
        request.setWorkflow(workflow);
        request.setWorkspace(workflow.getWorkspace());
        request.setEntityType(entityType);
        request.setEntityId(entityId);
        request.setRequester(requester);
        request.setCurrentStep(0);
        request.setStatus("PENDING");
        request.setRequestedAt(LocalDateTime.now());

        ApprovalRequest saved = requestRepository.save(request);

        createApprovalSteps(saved);

        WebSocketEvent<Map<String, Object>> event = WebSocketEvent.of("approval", "request_submitted",
                workflow.getWorkspace().getId(), "approval_request", saved.getId(),
                Map.of("id", saved.getId(), "entityType", entityType, "entityId", entityId, "status", "PENDING"));
        eventDispatcher.broadcastToWorkspace(workflow.getWorkspace().getId(), event);

        log.info("Submitted approval request: {} for {} {}", saved.getId(), entityType, entityId);
        return saved;
    }

    @Transactional
    public ApprovalRequest approveStep(Long stepId, Long approverId, String comments) {
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval step not found"));

        if (!step.getApprover().getId().equals(approverId)) {
            throw new BadRequestException("You are not the assigned approver for this step");
        }

        if (!"PENDING".equals(step.getStatus())) {
            throw new BadRequestException("This step has already been processed");
        }

        step.setStatus("APPROVED");
        step.setApprovedAt(LocalDateTime.now());
        step.setComments(comments);
        stepRepository.save(step);

        ApprovalRequest request = step.getRequest();
        request.setCurrentStep(request.getCurrentStep() + 1);

        List<ApprovalStep> steps = stepRepository.findByRequestIdOrderByStepNumberAsc(request.getId());
        ApprovalStep nextStep = steps.stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .findFirst()
                .orElse(null);

        if (nextStep == null) {
            request.setStatus("APPROVED");
            request.setCompletedAt(LocalDateTime.now());
            requestRepository.save(request);

            WebSocketEvent<Map<String, Object>> event = WebSocketEvent.of("approval", "request_approved",
                    request.getWorkspace().getId(), "approval_request", request.getId(),
                    Map.of("id", request.getId(), "status", "APPROVED"));
            eventDispatcher.broadcastToWorkspace(request.getWorkspace().getId(), event);

            log.info("Approval request {} fully approved", request.getId());
        } else {
            requestRepository.save(request);
        }

        return request;
    }

    @Transactional
    public ApprovalRequest rejectStep(Long stepId, Long approverId, String reason) {
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval step not found"));

        if (!step.getApprover().getId().equals(approverId)) {
            throw new BadRequestException("You are not the assigned approver for this step");
        }

        step.setStatus("REJECTED");
        step.setRejectedAt(LocalDateTime.now());
        step.setComments(reason);
        stepRepository.save(step);

        ApprovalRequest request = step.getRequest();
        request.setStatus("REJECTED");
        request.setRejectionReason(reason);
        request.setCompletedAt(LocalDateTime.now());
        requestRepository.save(request);

        WebSocketEvent<Map<String, Object>> event = WebSocketEvent.of("approval", "request_rejected",
                request.getWorkspace().getId(), "approval_request", request.getId(),
                Map.of("id", request.getId(), "status", "REJECTED", "reason", reason));
        eventDispatcher.broadcastToWorkspace(request.getWorkspace().getId(), event);

        log.info("Approval request {} rejected: {}", request.getId(), reason);
        return request;
    }

    public List<ApprovalRequest> getPendingRequests(Long workspaceId) {
        return requestRepository.findByWorkspaceIdAndStatus(workspaceId, "PENDING");
    }

    public List<ApprovalRequest> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId);
    }

    private void createApprovalSteps(ApprovalRequest request) {
        try {
            List<Map<String, Object>> chain = objectMapper.readValue(
                    request.getWorkflow().getApprovalChain(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );

            for (int i = 0; i < chain.size(); i++) {
                Map<String, Object> stepConfig = chain.get(i);
                Long approverId = Long.valueOf(stepConfig.get("approverId").toString());

                User approver = userRepository.findById(approverId)
                        .orElseThrow(() -> new ResourceNotFoundException("Approver not found: " + approverId));

                ApprovalStep step = new ApprovalStep();
                step.setRequest(request);
                step.setStepNumber(i + 1);
                step.setApprover(approver);
                step.setStatus("PENDING");
                stepRepository.save(step);
            }
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid approval chain configuration");
        }
    }

    private Workspace getWorkspace(Long workspaceId) {
        return null;
    }
}



