package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.dto.response.WorkloadBalanceResponse;
import com.vinncorp.erp.modules.projects.entity.MemberWorkload;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.repository.SprintCapacityRepository;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.service.TaskDependencyGraphService;
import com.vinncorp.erp.modules.projects.service.WorkloadBalancingService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkloadBalancingServiceImpl implements WorkloadBalancingService {

    private static final long CACHE_TTL = 300;

    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SprintCapacityRepository capacityRepository;
    private final TaskDependencyGraphService dependencyGraphService;
    private final CacheService cacheService;

    @Override
    public WorkloadBalanceResponse analyze(Long sprintId) {
        String cacheKey = CacheNames.sprintWorkload(sprintId);
        Optional<WorkloadBalanceResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprintId);
        Map<Long, List<Task>> memberTasks = new HashMap<>();
        Map<Long, Double> memberBlockedHours = new HashMap<>();

        for (TaskSprint ts : taskSprints) {
            Task task = ts.getTask();
            if (task == null) continue;
            if (task.getAssignee() == null) continue;

            Long assigneeId = task.getAssignee().getId();
            memberTasks.computeIfAbsent(assigneeId, k -> new ArrayList<>()).add(task);

            boolean blocked = false;
            try {
                var blockedStatus = dependencyGraphService.getBlockedStatus(task.getId());
                blocked = blockedStatus != null && blockedStatus.isBlocked();
            } catch (Exception ignored) {}
            if (blocked) {
                memberBlockedHours.merge(assigneeId, 1.0, Double::sum);
            }
        }

        double totalTasks = taskSprints.size();
        double idealShare = memberTasks.size() > 0 ? totalTasks / memberTasks.size() : 1;

        List<MemberWorkload> workloads = new ArrayList<>();
        for (Map.Entry<Long, List<Task>> entry : memberTasks.entrySet()) {
            Long uid = entry.getKey();
            List<Task> tasks = entry.getValue();
            User user = userRepository.findById(uid).orElse(null);

            double blockedPct = memberBlockedHours.getOrDefault(uid, 0.0) / Math.max(tasks.size(), 1) * 100;
            boolean overloaded = tasks.size() > idealShare * 1.3;
            boolean underutilized = tasks.size() < idealShare * 0.7;

            workloads.add(MemberWorkload.builder()
                    .userId(uid)
                    .userName(user != null ? user.getName() : "Unknown")
                    .taskCount(tasks.size())
                    .estimatedHours(tasks.size() * 1.0)
                    .assignedPoints(tasks.size())
                    .blockedWorkPercent(blockedPct)
                    .overloaded(overloaded)
                    .underutilized(underutilized)
                    .build());
        }

        int overloadedCount = (int) workloads.stream().filter(MemberWorkload::isOverloaded).count();
        int underutilizedCount = (int) workloads.stream().filter(MemberWorkload::isUnderutilized).count();

        double gini = computeGiniCoefficient(workloads.stream()
                .mapToDouble(MemberWorkload::getTaskCount).toArray());

        WorkloadBalanceResponse response = WorkloadBalanceResponse.builder()
                .sprintId(sprintId)
                .sprintName(sprint.getName())
                .members(workloads)
                .overloadedCount(overloadedCount)
                .underutilizedCount(underutilizedCount)
                .giniCoefficient(gini)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL);
        return response;
    }

    private double computeGiniCoefficient(double[] values) {
        if (values.length == 0) return 0;
        Arrays.sort(values);
        double sum = Arrays.stream(values).sum();
        if (sum == 0) return 0;
        double cumulative = 0;
        double gini = 0;
        int n = values.length;
        for (int i = 0; i < n; i++) {
            cumulative += values[i];
            gini += (i + 1) * values[i];
        }
        gini = (2 * gini / (n * sum)) - (n + 1.0) / n;
        return Math.max(0, gini);
    }

    @Override
    public void evictCache(Long sprintId) {
        cacheService.evict(CacheNames.sprintWorkload(sprintId));
    }
}



