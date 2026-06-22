import API from '../../../api/axios';
import { getUserProjects as getProjects } from './projectApi.js';

const normalizeMember = (member: any= {}) => ({
  id: member.id,
  userId: member.user?.id ?? member.userId ?? null,
  name: member.user?.name ?? member.name ?? '',
  email: member.user?.email ?? member.email ?? '',
  avatarUrl: member.user?.avatarUrl ?? member.avatarUrl ?? '',
  role: member.projectRole?.name ?? member.role?.name ?? member.role ?? 'TEAM_MEMBER',
  projectRole: member.projectRole ?? null,
});

export const getProjectMembers = async (projectId: any) => {
  if (!projectId) throw new Error('projectId required');
  try {
    const res = await API.get(`/projects/${projectId}/members`);
    const members = Array.isArray(res.data?.data)
      ? res.data.data
      : Array.isArray(res.data)
      ? res.data
      : [];
    return members.map(normalizeMember);
  } catch (error) {
    console.error('Get project members error:', error);
    return [];
  }
};

export const addProjectMember = async (projectId: any, data: any) => {
  if (!projectId || !data.userId || !data.role) {
    throw new Error('projectId, userId, role required');
  }
  try {
    const res = await API.post(`/projects/${projectId}/members`, data);
    return normalizeMember(res.data?.data || res.data);
  } catch (error) {
    console.error('Add member error:', error);
    throw error;
  }
};

export const updateProjectMemberRole = async (projectId: any, memberUserId: any, role: any) => {
  if (!projectId || !memberUserId || !role) {
    throw new Error('projectId, memberUserId, role required');
  }
  try {
    const res = await API.put(`/projects/${projectId}/members/${memberUserId}/role`, {
      role,
    });
    return normalizeMember(res.data?.data || res.data);
  } catch (error) {
    console.error('Update role error:', error);
    throw error;
  }
};

export const removeProjectMember = async (projectId: any, memberUserId: any) => {
  if (!projectId || !memberUserId) {
    throw new Error('projectId, memberUserId required');
  }
  try {
    const res = await API.delete(`/projects/${projectId}/members/${memberUserId}`);
    return res.data;
  } catch (error) {
    console.error('Remove member error:', error);
    throw error;
  }
};

export const bulkAddProjectMembers = async (projectId: any, members: any) => {
  if (!projectId || !Array.isArray(members) || members.length === 0) {
    throw new Error('projectId and non-empty members array required');
  }
  try {
    const res = await API.post(`/projects/${projectId}/members/bulk`, { members });
    const created = Array.isArray(res.data?.data)
      ? res.data.data
      : Array.isArray(res.data)
      ? res.data
      : [];
    return created.map(normalizeMember);
  } catch (error) {
    console.error('Bulk add error:', error);
    throw error;
  }
};

export const searchProjectMembers = async (projectId: any, role: any= '', search: any= '') => {
  const params = new URLSearchParams();
  if (role) params.append('role', role);
  if (search) params.append('search', search);
  try {
    const res = await API.get(`/projects/${projectId}/members/search?${params}`);
    const members = Array.isArray(res.data?.data)
      ? res.data.data
      : Array.isArray(res.data)
      ? res.data
      : [];
    return members.map(normalizeMember);
  } catch (error) {
    console.error('Search members error:', error);
    return [];
  }
};

export const getUserProjects = async () => getProjects();

export const inviteAssigneeByEmail = async (projectId: any, email: any, role: any = 'TEAM_MEMBER') => {
  try {
    const res = await API.post(`/projects/${projectId}/members/invite`, { email, role });
    return normalizeMember(res.data?.data || res.data);
  } catch (error) {
    console.error('Invite assignee error:', error);
    throw error;
  }
};
