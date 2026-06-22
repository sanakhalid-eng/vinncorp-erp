import API from '../../../api/axios.js';

export const getMyPermissions = async (projectId: any) => {
  if (!projectId) return [];
  try {
    const res = await API.get(`/projects/${projectId}/members/me/permissions`);
    const data = res.data;
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error('Get my permissions error:', error);
    return [];
  }
};
