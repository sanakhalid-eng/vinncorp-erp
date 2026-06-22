package com.vinncorp.erp.shared.scheduling;

import com.vinncorp.erp.modules.projects.entity.Label;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
import com.vinncorp.erp.modules.projects.repository.LabelRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsistencyRepairJob {

    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;
    private final WorkflowStatusRepository workflowStatusRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void repairAllCounters() {
        log.info("Starting consistency repair job");

        int repairedParents = repairParentTaskCounters();
        int repairedLabels = repairLabelUsageCounters();

        log.info("Consistency repair job completed: {} parent tasks repaired, {} labels repaired", repairedParents, repairedLabels);
    }

    private int repairParentTaskCounters() {
        List<Task> parentTasks = taskRepository.findAll().stream()
                .filter(t -> t.getSubtaskCount() > 0 || taskRepository.countByParentTaskId(t.getId()) > 0)
                .toList();

        int repaired = 0;
        for (Task parent : parentTasks) {
            long actualTotal = taskRepository.countByParentTaskId(parent.getId());

            Long doneStatusId = parent.getProject() != null
                    ? workflowStatusRepository.findByProjectIdAndName(parent.getProject().getId(), "DONE")
                            .map(WorkflowStatus::getId)
                            .orElse(null)
                    : null;

            long actualCompleted = doneStatusId != null
                    ? taskRepository.countByParentTaskIdAndStatusEntity_Id(parent.getId(), doneStatusId)
                    : 0;

            if (parent.getSubtaskCount() != actualTotal || parent.getCompletedSubtaskCount() != actualCompleted) {
                log.warn("Repairing parent task {}: subtaskCount {}→{}, completedSubtaskCount {}→{}",
                        parent.getId(), parent.getSubtaskCount(), actualTotal,
                        parent.getCompletedSubtaskCount(), actualCompleted);

                parent.setSubtaskCount((int) actualTotal);
                parent.setCompletedSubtaskCount((int) actualCompleted);
                taskRepository.save(parent);
                repaired++;
            }
        }

        return repaired;
    }

    private int repairLabelUsageCounters() {
        List<Label> allLabels = labelRepository.findAll();

        int repaired = 0;
        for (Label label : allLabels) {
            long actualUsage = labelRepository.countAllUsageByLabelId(label.getId());

            if (label.getUsageCount() != actualUsage) {
                log.warn("Repairing label {}: usageCount {}→{}",
                        label.getId(), label.getUsageCount(), actualUsage);

                label.setUsageCount((int) actualUsage);
                labelRepository.save(label);
                repaired++;
            }
        }

        return repaired;
    }
}



