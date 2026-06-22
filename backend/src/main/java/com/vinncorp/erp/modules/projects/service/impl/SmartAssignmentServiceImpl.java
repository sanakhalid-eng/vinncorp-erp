package com.vinncorp.erp.modules.projects.service.impl;
import com.vinncorp.erp.platform.user.entity.User;

import com.vinncorp.erp.platform.user.repository.UserRepository;

import com.vinncorp.erp.modules.projects.dto.response.SmartAssignmentResponse;
import com.vinncorp.erp.modules.projects.entity.ProjectMember;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.SmartAssignmentService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartAssignmentServiceImpl implements SmartAssignmentService {

    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SmartAssignmentResponse autoAssign(Long taskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        Long projectId = task.getProject().getId();
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);

        if (members.isEmpty()) {
            throw new IllegalStateException("No project members available for assignment");
        }

        ProjectMember bestMember = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        List<String> factors = new ArrayList<>();

        for (ProjectMember member : members) {
            if (member.getUser() == null) continue;
            double score = 0;

            long activeTaskCount = taskRepository.countByAssigneeIdAndProjectIdAndActive(
                    member.getUser().getId(), projectId);
            double workloadFactor = Math.max(0, 10 - activeTaskCount);
            score += workloadFactor * 3;
            if (activeTaskCount <= 3) {
                factors.add("Low workload (" + activeTaskCount + " active tasks)");
            }

            long overdueCount = taskRepository.countByAssigneeIdAndDueDateBeforeAndCompletedFalse(
                    member.getUser().getId(), LocalDateTime.now());
            score -= overdueCount * 2;
            if (overdueCount == 0) {
                factors.add("No overdue tasks");
            }

            if (bestMember == null || score > bestScore) {
                bestScore = score;
                bestMember = member;
            }
        }

        if (bestMember == null) {
            bestMember = members.getFirst();
            bestScore = 50;
            factors.add("Default assignment (first available member)");
        }

        User assignee = bestMember.getUser();
        task.setAssignee(assignee);
        taskRepository.save(task);

        double confidenceScore = Math.min(100, Math.max(0, bestScore * 5));

        return SmartAssignmentResponse.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .assignedUserId(assignee.getId())
                .assignedUserName(assignee.getName())
                .reason("Best match based on workload, capacity, and overdue analysis")
                .factors(factors)
                .confidenceScore(confidenceScore)
                .build();
    }
}



