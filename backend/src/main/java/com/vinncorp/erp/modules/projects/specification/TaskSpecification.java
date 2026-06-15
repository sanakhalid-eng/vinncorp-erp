package com.vinncorp.erp.modules.projects.specification;

import com.vinncorp.erp.modules.projects.dto.request.TaskFilterRequest;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.entity.TaskLabel;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class TaskSpecification {

    public static Specification<Task> hasProjectId(Long projectId) {
        return (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> inSprint(Boolean inSprint) {
        return (root, query, cb) -> {
            if (inSprint == null) return cb.conjunction();
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<TaskSprint> tsRoot = subquery.from(TaskSprint.class);
            subquery.select(tsRoot.get("task").get("id"));
            if (inSprint) {
                return cb.in(root.get("id")).value(subquery);
            } else {
                return cb.not(cb.in(root.get("id")).value(subquery));
            }
        };
    }

    public static Specification<Task> hasSprintId(Long sprintId) {
        return (root, query, cb) -> {
            if (sprintId == null) return cb.conjunction();
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<TaskSprint> tsRoot = subquery.from(TaskSprint.class);
            subquery.select(tsRoot.get("task").get("id"))
                    .where(cb.equal(tsRoot.get("sprint").get("id"), sprintId));
            return cb.in(root.get("id")).value(subquery);
        };
    }

    public static Specification<Task> hasLabelIds(List<Long> labelIds) {
        return (root, query, cb) -> {
            if (labelIds == null || labelIds.isEmpty()) return cb.conjunction();
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<TaskLabel> tlRoot = subquery.from(TaskLabel.class);
            subquery.select(tlRoot.get("task").get("id"))
                    .where(tlRoot.get("label").get("id").in(labelIds));
            return cb.in(root.get("id")).value(subquery);
        };
    }

    public static Specification<Task> hasStatusId(Long statusId) {
        return (root, query, cb) -> {
            if (statusId == null) return cb.conjunction();
            return cb.equal(root.get("statusEntity").get("id"), statusId);
        };
    }

    public static Specification<Task> hasAssigneeId(Long assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) return cb.conjunction();
            return cb.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    public static Specification<Task> hasPriority(String priority) {
        return (root, query, cb) -> {
            if (priority == null || priority.isEmpty()) return cb.conjunction();
            return cb.equal(root.get("priority"), priority);
        };
    }

    public static Specification<Task> isBlocked() {
        return (root, query, cb) -> {
            Subquery<Long> depSubquery = query.subquery(Long.class);
            Root<TaskDependency> depRoot = depSubquery.from(TaskDependency.class);
            depSubquery.select(depRoot.get("task").get("id"));
            return cb.in(root.get("id")).value(depSubquery);
        };
    }

    public static Specification<Task> hasSubtasks(Boolean hasSubtasks) {
        return (root, query, cb) -> {
            if (hasSubtasks == null) return cb.conjunction();
            if (hasSubtasks) {
                return cb.greaterThan(root.get("subtaskCount"), 0);
            } else {
                return cb.equal(root.get("subtaskCount"), 0);
            }
        };
    }

    public static Specification<Task> build(TaskFilterRequest filter, Long projectId) {
        Specification<Task> spec = hasProjectId(projectId);
        if (filter.getSprintId() != null) {
            spec = spec.and(hasSprintId(filter.getSprintId()));
        } else if (filter.getInSprint() != null) {
            spec = spec.and(inSprint(filter.getInSprint()));
        }
        if (filter.getLabelIds() != null && !filter.getLabelIds().isEmpty()) {
            spec = spec.and(hasLabelIds(filter.getLabelIds()));
        }
        if (filter.getStatusId() != null) {
            spec = spec.and(hasStatusId(filter.getStatusId()));
        }
        if (filter.getAssigneeId() != null) {
            spec = spec.and(hasAssigneeId(filter.getAssigneeId()));
        }
        if (filter.getPriority() != null) {
            spec = spec.and(hasPriority(filter.getPriority()));
        }
        if (filter.getHasSubtasks() != null) {
            spec = spec.and(hasSubtasks(filter.getHasSubtasks()));
        }
        return spec;
    }
}



