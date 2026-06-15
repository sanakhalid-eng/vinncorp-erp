package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowStatusRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowTransitionRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowTransitionRuleRepository;
import com.vinncorp.erp.modules.projects.service.PermissionService;
import com.vinncorp.erp.modules.projects.service.TaskDependencyService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowValidator {

    private final WorkflowTransitionRepository transitionRepository;
    private final WorkflowTransitionRuleRepository ruleRepository;
    private final UserRepository userRepository;
    private final WorkflowRuleEngine ruleEngine;
    private final PermissionService permissionService;
    private final TaskRepository taskRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final TaskDependencyService taskDependencyService;

    public void validate(Task task,
                         Long fromStatusId,
                         Long toStatusId,
                         String email) {

        if (fromStatusId.equals(toStatusId)) {
            throw new BadRequestException("Cannot transition to same status");
        }

        Long projectId = task.getProject().getId();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        WorkflowStatus toStatus = workflowStatusRepository.findById(toStatusId)
                .orElseThrow(() -> new BadRequestException("Target status not found"));

        if (isDoneStatus(toStatus.getName())) {
            validateNoIncompleteSubtasks(task.getId());
            taskDependencyService.validateDependenciesBeforeStatusChange(task.getId());
        }

        if (isInProgressStatus(toStatus.getName())) {
            taskDependencyService.validateDependenciesBeforeStatusChange(task.getId());
        }

        // 1. STRUCTURAL RULE - transition must exist
        boolean transitionExists =
                transitionRepository.existsByProjectIdAndFromStatusIdAndToStatusId(
                        projectId, fromStatusId, toStatusId
                );

        if (!transitionExists) {
            throw new BadRequestException("Transition not defined in workflow");
        }

        // 2. PERMISSION RULE - check required_permissions field
        String requiredPermissionsJson = ruleRepository
                .findRequiredPermissions(projectId, fromStatusId, toStatusId)
                .stream()
                .findFirst()
                .orElse(null);

        if (requiredPermissionsJson != null && !requiredPermissionsJson.isBlank()) {
            String[] permissions = requiredPermissionsJson.replaceAll("[\\[\\]\"]", "").split(",");
            boolean hasAnyPermission = false;
            for (String perm : permissions) {
                String trimmed = perm.trim();
                if (!trimmed.isEmpty() && permissionService.hasPermission(user.getId(), projectId, trimmed)) {
                    hasAnyPermission = true;
                    break;
                }
            }
            if (!hasAnyPermission) {
                throw new BadRequestException("You do not have the required permissions for this transition");
            }
        }

        // 3. BUSINESS RULES (ruleJson engine)
        String ruleJson = ruleRepository
                .findRuleJson(projectId, fromStatusId, toStatusId)
                .stream()
                .findFirst()
                .orElse(null);

        boolean allowedByEngine = ruleEngine.evaluate(ruleJson, task, user);
        if (!allowedByEngine) {
            throw new BadRequestException("Blocked by business rules");
        }
    }

    private boolean isDoneStatus(String statusName) {
        String normalized = statusName.toUpperCase().trim();
        return normalized.equals("DONE") || normalized.equals("COMPLETED") || normalized.equals("CLOSED");
    }

    private boolean isInProgressStatus(String statusName) {
        String normalized = statusName.toUpperCase().trim();
        return normalized.equals("IN_PROGRESS") || normalized.equals("IN PROGRESS") || normalized.equals("INPROGRESS");
    }

    private void validateNoIncompleteSubtasks(Long taskId) {
        long totalSubtasks = taskRepository.countByParentTaskId(taskId);
        if (totalSubtasks == 0) return;

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || task.getProject() == null) return;

        WorkflowStatus doneStatus = workflowStatusRepository.findByProjectIdAndName(task.getProject().getId(), "DONE")
                .orElse(null);

        if (doneStatus != null) {
            long completedSubtasks = taskRepository.countByParentTaskIdAndStatusEntity_Id(taskId, doneStatus.getId());
            if (completedSubtasks < totalSubtasks) {
                throw new BadRequestException(
                        String.format("Cannot move to DONE: %d of %d subtasks are incomplete",
                                totalSubtasks - completedSubtasks, totalSubtasks)
                );
            }
        }
    }
}



