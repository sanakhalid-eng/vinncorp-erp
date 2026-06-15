package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskSecurity {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Long getProjectId(Long taskId) {
        return taskRepository.findById(taskId)
                .map(task -> task.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    public boolean isAssignee(Long taskId, String email) {

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        return taskRepository.findById(taskId)
                .map(task -> task.getAssignee() != null &&
                        task.getAssignee().getId().equals(user.getId()))
                .orElse(false);
    }
}

