package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;

import com.vinncorp.erp.modules.projects.dto.response.CapacityResponse;
import com.vinncorp.erp.modules.projects.dto.response.CapacitySummaryResponse;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.SprintCapacity;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.repository.SprintCapacityRepository;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.service.CapacityService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CapacityServiceImpl implements CapacityService {

    private static final long CACHE_TTL = 300;

    private final SprintCapacityRepository capacityRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final TaskRepository taskRepository;
    private final CacheService cacheService;

    @Override
    @Transactional
    public CapacityResponse setCapacity(Long sprintId, Long userId, double availableHours, int ptoDays, String email) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (availableHours <= 0) {
            throw new BadRequestException("Available hours must be positive");
        }
        if (ptoDays < 0) {
            throw new BadRequestException("PTO days cannot be negative");
        }

        double allocatedHours = computeAllocatedHours(sprintId, userId);

        SprintCapacity capacity = capacityRepository.findBySprintIdAndUserId(sprintId, userId)
                .orElse(new SprintCapacity());

        capacity.setSprintId(sprintId);
        capacity.setUserId(userId);
        capacity.setWorkspaceId(sprint.getProject().getWorkspace().getId());
        capacity.setAvailableHours(availableHours);
        capacity.setAllocatedHours(allocatedHours);
        capacity.setUtilizationPercent(availableHours > 0 ? (allocatedHours / availableHours) * 100 : 0);
        capacity.setPtoDays(ptoDays);

        SprintCapacity saved = capacityRepository.save(capacity);
        evictCache(sprintId);

        return toCapacityResponse(saved, user);
    }

    @Override
    public CapacityResponse getMemberCapacity(Long sprintId, Long userId) {
        SprintCapacity capacity = capacityRepository.findBySprintIdAndUserId(sprintId, userId)
                .orElse(null);
        if (capacity == null) {
            double allocated = computeAllocatedHours(sprintId, userId);
            return CapacityResponse.builder()
                    .sprintId(sprintId)
                    .userId(userId)
                    .userName(resolveUserName(userId))
                    .availableHours(0)
                    .allocatedHours(allocated)
                    .utilizationPercent(0)
                    .ptoDays(0)
                    .overCapacity(false)
                    .build();
        }
        User user = userRepository.findById(userId).orElse(null);
        return toCapacityResponse(capacity, user);
    }

    @Override
    public List<CapacityResponse> getSprintCapacities(Long sprintId) {
        return capacityRepository.findBySprintId(sprintId).stream()
                .map(c -> {
                    User u = userRepository.findById(c.getUserId()).orElse(null);
                    return toCapacityResponse(c, u);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CapacitySummaryResponse getCapacitySummary(Long sprintId) {
        evictCache(sprintId);
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        List<CapacityResponse> members = getSprintCapacities(sprintId);

        double totalAvailable = members.stream().mapToDouble(CapacityResponse::getAvailableHours).sum();
        double totalAllocated = members.stream().mapToDouble(CapacityResponse::getAllocatedHours).sum();
        double avgUtil = members.isEmpty() ? 0 :
                members.stream().mapToDouble(CapacityResponse::getUtilizationPercent).average().orElse(0);
        int overCount = (int) members.stream().filter(CapacityResponse::isOverCapacity).count();
        int underCount = (int) members.stream().filter(m -> !m.isOverCapacity() && m.getUtilizationPercent() < 50).count();

        return CapacitySummaryResponse.builder()
                .sprintId(sprintId)
                .sprintName(sprint.getName())
                .totalMembers(members.size())
                .totalAvailableHours(totalAvailable)
                .totalAllocatedHours(totalAllocated)
                .averageUtilization(avgUtil)
                .overCapacityCount(overCount)
                .underUtilizedCount(underCount)
                .members(members)
                .build();
    }

    @Override
    @Transactional
    public void recalculateAllocation(Long sprintId) {
        List<SprintCapacity> capacities = capacityRepository.findBySprintId(sprintId);
        for (SprintCapacity cap : capacities) {
            double allocated = computeAllocatedHours(sprintId, cap.getUserId());
            cap.setAllocatedHours(allocated);
            cap.setUtilizationPercent(cap.getAvailableHours() > 0
                    ? (allocated / cap.getAvailableHours()) * 100 : 0);
            capacityRepository.save(cap);
        }
        evictCache(sprintId);
    }

    private double computeAllocatedHours(Long sprintId, Long userId) {
        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintId(sprintId);
        double total = 0;
        for (TaskSprint ts : taskSprints) {
            if (ts.getTask() != null && ts.getTask().getAssignee() != null
                    && ts.getTask().getAssignee().getId().equals(userId)) {
                total += 1.0;
            }
        }
        return total;
    }

    private String resolveUserName(Long userId) {
        return userRepository.findById(userId).map(User::getName).orElse("Unknown");
    }

    private CapacityResponse toCapacityResponse(SprintCapacity c, User u) {
        return CapacityResponse.builder()
                .id(c.getId())
                .sprintId(c.getSprintId())
                .userId(c.getUserId())
                .userName(u != null ? u.getName() : resolveUserName(c.getUserId()))
                .userEmail(u != null ? u.getEmail() : null)
                .availableHours(c.getAvailableHours())
                .allocatedHours(c.getAllocatedHours())
                .utilizationPercent(c.getUtilizationPercent())
                .ptoDays(c.getPtoDays())
                .overCapacity(c.getUtilizationPercent() > 100)
                .build();
    }

    @Override
    public void evictCache(Long sprintId) {
        cacheService.evict(CacheNames.sprintCapacity(sprintId));
    }
}



