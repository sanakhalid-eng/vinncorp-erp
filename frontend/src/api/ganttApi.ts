import API from './axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

export const getGanttData = async (projectId: any) => {
  try {
    const res = await API.get(`/projects/${projectId}/gantt`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get Gantt data error:', error);
    throw error;
  }
};
