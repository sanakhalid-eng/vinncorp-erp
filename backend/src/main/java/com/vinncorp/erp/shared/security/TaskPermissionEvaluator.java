package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.platform.user.constants.PermissionConstants;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskPermissionEvaluator {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PermissionSecurity permissionSecurity;


    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public boolean canUpdateTask(Long taskId, String email) {

        Task task = getTask(taskId);
        User user = getUser(email);
        Long projectId = task.getProject().getId();

        if (permissionSecurity.hasPermission(projectId, email, PermissionConstants.EDIT_TASK)) {
            return true;
        }

        if (task.getAssignee() != null &&
                task.getAssignee().getId().equals(user.getId())) {
            return true;
        }

        return task.getCreator() != null &&
                task.getCreator().getId().equals(user.getId());
    }

    public boolean canDeleteTask(Long taskId, String email) {

        Task task = getTask(taskId);
        Long projectId = task.getProject().getId();

        return permissionSecurity.hasPermission(
                projectId, email, PermissionConstants.DELETE_TASK
        );
    }

    public boolean canViewTask(Long taskId, String email) {

        Task task = getTask(taskId);
        Long projectId = task.getProject().getId();

        return permissionSecurity.hasPermission(
                projectId, email, PermissionConstants.VIEW_TASK
        );
    }

    public boolean canUpdatePriority(Long taskId, String email) {

        Task task = getTask(taskId);
        Long projectId = task.getProject().getId();

        return permissionSecurity.hasPermission(
                projectId, email, PermissionConstants.EDIT_TASK
        );
    }

    public boolean canUpdateStatus(Long taskId, String email) {

        Task task = getTask(taskId);
        User user = getUser(email);
        Long projectId = task.getProject().getId();

        if (permissionSecurity.hasPermission(projectId, email, PermissionConstants.EDIT_TASK)) {
            return true;
        }

        return task.getAssignee() != null &&
                task.getAssignee().getId().equals(user.getId());
    }

    public boolean canUpdateBasicFields(Long taskId, String email) {

        Task task = getTask(taskId);
        User user = getUser(email);
        Long projectId = task.getProject().getId();

        if (permissionSecurity.hasPermission(projectId, email, PermissionConstants.EDIT_TASK)) {
            return true;
        }

        return task.getCreator() != null &&
                task.getCreator().getId().equals(user.getId());
    }
}



