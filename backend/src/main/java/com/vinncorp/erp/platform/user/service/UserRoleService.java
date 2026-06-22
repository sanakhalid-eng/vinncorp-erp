package com.vinncorp.erp.platform.user.service;

import com.vinncorp.erp.platform.user.entity.UserRole;

import java.util.List;

public interface UserRoleService {

    void assignSystemRole(Long userId, Long roleId);

    void removeSystemRole(Long userId, Long roleId);

    List<UserRole> getUserSystemRoles(Long userId);
}

