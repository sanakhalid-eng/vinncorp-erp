package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.core.user.entity.User;

public interface MembershipResolver {

    boolean isWorkspaceOwner(User user);

    boolean isWorkspaceOwner(String email);

    boolean isWorkspaceOwner(Long userId);

    User resolveWorkspaceOwner();

    Long resolveWorkspaceOwnerId();

    boolean canAssignAdminRole(User currentUser);

    boolean isAdmin(User user);

    boolean isSuperAdmin(User user);

    boolean isSuperAdmin(String email);
}

