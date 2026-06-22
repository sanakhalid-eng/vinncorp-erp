import API from '../../../api/axios';

/**
 * Role Management API
 * Backend: /api/roles (system roles CRUD)
 * Assignment: /api/projects/{projectId}/members/{userId}/role (project-scoped)
 * System roles: /api/users/{userId}/system-role (1 per user enforced)
 */

export const getAllRoles = async (scope: any= null) => {
  try {
    const res = await API.get('/roles', {
      params: scope ? { scope } : {}
    });
    const roles = Array.isArray(res.data?.data)
      ? res.data.data
      : Array.isArray(res.data)
      ? res.data
      : [];
    // Filter by scope on frontend if backend doesn't support it
    return scope ? roles.filter((role: any) => role.scope === scope) : roles;
  } catch (error) {
    console.error('Get all roles error:', error);
    throw error;
  }
};

export const createRole = async (data: any) => {
  if (!data.name) {
    throw new Error('Role name required');
  }
  try {
    const res = await API.post('/roles', data);
    return res.data;
  } catch (error) {
    console.error('Create role error:', error);
    throw error;
  }
};

export const updateRole = async (id: any, data: any) => {
  if (!id || !data.name) {
    throw new Error('Role ID and name required');
  }
  try {
    const res = await API.put(`/roles/${id}`, data);
    return res.data;
  } catch (error) {
    console.error('Update role error:', error);
    throw error;
  }
};

export const deleteRole = async (id: any) => {
  if (!id) {
    throw new Error('Role ID required');
  }
  try {
    const res = await API.delete(`/roles/${id}`);
    return res.data;
  } catch (error) {
    console.error('Delete role error:', error);
    throw error;
  }
};

export const getRolePermissionsAsync = async (roleId: any) => {
  if (!roleId) {
    throw new Error('Role ID required');
  }
  try {
    const res = await API.get(`/roles/${roleId}/permissions`);
    return res.data.permissions || [];
  } catch (error) {
    console.error('Get role permissions error:', error);
    throw error;
  }
};

export const updateRolePermissions = async (roleId: any, permissionIds: any) => {
  if (!roleId || !Array.isArray(permissionIds)) {
    throw new Error('Role ID and permissionIds array required');
  }
  try {
    const res = await API.put(`/roles/${roleId}/permissions`, { permissions: permissionIds });
    return res.data;
  } catch (error) {
    console.error('Update role permissions error:', error);
    throw error;
  }
};


// Project member role assignment (reuse pattern)
export const updateProjectMemberRole = async (projectId: any, userId: any, role: any) => {
  if (!projectId || !userId || !role) {
    throw new Error('projectId, userId, role required');
  }
  try {
    const res = await API.put(`/projects/${projectId}/members/${userId}/role`, { role });
    return res.data.data;
  } catch (error) {
    console.error('Update project member role error:', error);
    throw error;
  }
};

// System role assignment (1 per user enforced by backend)
export const assignSystemRole = async (userId: any, roleId: any) => {
  if (!userId || !roleId) {
    throw new Error('userId and roleId required');
  }
  try {
    const res = await API.post(`/users/${userId}/system-role`, null, { params: { roleId } });
    return res.data;
  } catch (error) {
    console.error('Assign system role error:', error);
    throw error;
  }
};

export const removeSystemRole = async (userId: any, roleId: any) => {
  if (!userId || !roleId) {
    throw new Error('userId and roleId required');
  }
  try {
    const res = await API.delete(`/users/${userId}/system-role`, { params: { roleId } });
    return res.data;
  } catch (error) {
    console.error('Remove system role error:', error);
    throw error;
  }
};

export const getUserSystemRoles = async (userId: any) => {
  if (!userId) {
    throw new Error('userId required');
  }
  try {
    const res = await API.get(`/users/${userId}/system-roles`);
    return res.data || [];
  } catch (error) {
    console.error('Get user system roles error:', error);
    throw error;
  }
};

export const getProjectRoles = async () => {
  try {
    const res = await API.get('/users/project-roles');
    return res.data;
  } catch (error) {
    console.error('Get project roles error:', error);
    throw error;
  }
};

