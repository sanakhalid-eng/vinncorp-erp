package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkflowRuleEngine {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public boolean evaluate(String ruleJson, Task task, User user) {

        if (ruleJson == null || ruleJson.isBlank()) {
            return true;
        }

        JSONObject rule = new JSONObject(ruleJson);

        return evaluateRule(rule, task, user);
    }

    private boolean evaluateRule(JSONObject rule, Task task, User user) {

        // AND condition
        if (rule.has("and")) {
            JSONArray arr = rule.getJSONArray("and");
            for (int i = 0; i < arr.length(); i++) {
                if (!evaluateRule(arr.getJSONObject(i), task, user)) {
                    return false;
                }
            }
            return true;
        }

        // PERMISSION check (preferred over role)
        if (rule.has("requiredPermissions")) {
            JSONArray perms = rule.getJSONArray("requiredPermissions");
            for (int i = 0; i < perms.length(); i++) {
                String permName = perms.getString(i);
                boolean hasPerm = projectMemberRepository
                        .hasPermission(task.getProject().getId(), user.getId(), permName);
                if (hasPerm) {
                    return true;
                }
            }
            return false;
        }

        // ROLE check (legacy, kept for backward compatibility)
        if (rule.has("role")) {
            String role = rule.getString("role");

            return projectMemberRepository
                    .findByProject_IdAndUser_Id(
                            task.getProject().getId(),
                            user.getId()
                    )
                    .map(m -> m.getRole().getName().equals(role))
                    .orElse(false);
        }

        // ASSIGNEE ONLY
        if (rule.has("assigneeOnly")) {
            return task.getAssignee() != null &&
                    task.getAssignee().getId().equals(user.getId());
        }

        // FIELD check (priority etc.)
        if (rule.has("field")) {
            String field = rule.getString("field");
            String value = rule.getString("value");

            if (field.equals("priority")) {
                return task.getPriority().name().equals(value);
            }

            if (field.equals("status")) {
                return task.getStatusEntity().getName().equals(value);
            }
        }

        return false;
    }
}



