package com.vinncorp.erp.platform.user.service;

import com.vinncorp.erp.modules.projects.entity.Role;

import java.util.List;

public interface  RoleService {

    Role createRole(Role role);

    List<Role> getAllRoles();

    Role updateRole(Long id, Role updated);

    void deleteRole(Long id);
}

