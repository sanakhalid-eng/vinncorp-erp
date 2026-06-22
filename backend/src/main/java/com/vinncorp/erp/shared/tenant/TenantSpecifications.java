package com.vinncorp.erp.shared.tenant;

import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

public final class TenantSpecifications {

    private TenantSpecifications() {}

    public static <T> Specification<T> belongsToWorkspace(Long workspaceId) {
        return (root, query, cb) -> {
            Path<Workspace> workspacePath = root.get("workspace");
            return cb.equal(workspacePath.get("id"), workspaceId);
        };
    }

    public static <T> Specification<T> belongsToCurrentWorkspace(CurrentWorkspaceResolver resolver) {
        Long workspaceId = resolver.getCurrentWorkspaceId();
        if (workspaceId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return belongsToWorkspace(workspaceId);
    }

    public static <T> Specification<T> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    @SuppressWarnings("unchecked")
    public static <T> Specification<T> tenantFilter(CurrentWorkspaceResolver resolver) {
        Specification<T> wsSpec = belongsToCurrentWorkspace(resolver);
        Specification<T> ndSpec = notDeleted();
        return wsSpec.and(ndSpec);
    }
}

