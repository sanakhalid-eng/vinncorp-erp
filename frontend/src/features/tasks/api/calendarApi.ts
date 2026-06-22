import API from '../../../api/axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

export const getCalendarData = async (projectId: any) => {
  try {
    const res = await API.get(`/projects/${projectId}/calendar`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get calendar data error:', error);
    throw error;
  }
};
