package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.projects.dto.response.CapacityForecastResponse;
import com.vinncorp.erp.modules.projects.entity.CapacityForecastSnapshot;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.SprintCapacity;
import com.vinncorp.erp.modules.projects.repository.CapacityForecastSnapshotRepository;
import com.vinncorp.erp.modules.projects.repository.SprintCapacityRepository;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.service.CapacityForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CapacityForecastServiceImpl implements CapacityForecastService {

    private static final long CACHE_TTL_MS = 300_000L;

    private final SprintCapacityRepository sprintCapacityRepository;
    private final CapacityForecastSnapshotRepository snapshotRepository;
    private final SprintRepository sprintRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CacheService cacheService;

    @Override
    @Transactional
    public CapacityForecastResponse forecastForSprint(Long workspaceId, Long sprintId) {
        String cacheKey = CacheNames.sprintCapacity(sprintId) + ":forecast";
        Optional<CapacityForecastResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        if (!workspaceId.equals(sprint.getProject().getWorkspace().getId())) {
            throw new ResourceNotFoundException("Sprint not found");
        }

        List<SprintCapacity> capacities = sprintCapacityRepository.findBySprintId(sprintId);
        double totalAvailable = capacities.stream().mapToDouble(SprintCapacity::getAvailableHours).sum();
        double totalAllocated = capacities.stream().mapToDouble(SprintCapacity::getAllocatedHours).sum();
        double utilization = totalAvailable > 0 ? (totalAllocated / totalAvailable) * 100 : 0;
        int overloadMembers = (int) capacities.stream()
                .filter(c -> c.getUtilizationPercent() > 100).count();
        double recommended = totalAvailable > 0 ? totalAllocated * 1.1 : 40;

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        CapacityForecastSnapshot snapshot = new CapacityForecastSnapshot();
        snapshot.setWorkspace(workspace);
        snapshot.setProjectId(sprint.getProject().getId());
        snapshot.setSprintId(sprintId);
        snapshot.setPredictedUtilization(utilization);
        snapshot.setPredictedOverloadMembers(overloadMembers);
        snapshot.setRecommendedCapacityHours(recommended);
        snapshot.setForecastHorizonDays(14);
        snapshotRepository.save(snapshot);

        CapacityForecastResponse response = CapacityForecastResponse.builder()
                .projectId(sprint.getProject().getId())
                .sprintId(sprintId)
                .predictedUtilization(utilization)
                .predictedOverloadMembers(overloadMembers)
                .recommendedCapacityHours(recommended)
                .forecastHorizonDays(14)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL_MS);
        return response;
    }
}



