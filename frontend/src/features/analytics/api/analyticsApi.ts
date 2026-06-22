import API from '../../../api/axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

export const getAnalyticsDashboard = async (projectId: any) => {
  try {
    const res = await API.get(`/projects/${projectId}/analytics/dashboard`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get analytics dashboard error:', error);
    throw error;
  }
};

export const getTimeAnalytics = async (projectId: any) => {
  try {
    const res = await API.get(`/projects/${projectId}/analytics/time`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get time analytics error:', error);
    throw error;
  }
};
