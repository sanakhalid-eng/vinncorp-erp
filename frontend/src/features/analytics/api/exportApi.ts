import API from '../../../api/axios';

export const exportTasks = async (projectId: any, format: any= 'csv') => {
  try {
    const response = await API.get(`/projects/${projectId}/export/tasks`, {
      params: { format },
      responseType: 'blob'
    });
    return response.data;
  } catch (error) {
    console.error('Export tasks error:', error);
    throw error;
  }
};

export const exportSprintReport = async (sprintId: any, format: any= 'pdf') => {
  try {
    const response = await API.get(`/sprints/${sprintId}/export`, {
      params: { format },
      responseType: 'blob'
    });
    return response.data;
  } catch (error) {
    console.error('Export sprint report error:', error);
    throw error;
  }
};

export const exportAnalytics = async (projectId: any, format: any= 'pdf') => {
  try {
    const response = await API.get(`/projects/${projectId}/export/analytics`, {
      params: { format },
      responseType: 'blob'
    });
    return response.data;
  } catch (error) {
    console.error('Export analytics error:', error);
    throw error;
  }
};

export const exportCalendar = async (projectId: any, format: any= 'csv') => {
  try {
    const response = await API.get(`/projects/${projectId}/export/calendar`, {
      params: { format },
      responseType: 'blob'
    });
    return response.data;
  } catch (error) {
    console.error('Export calendar error:', error);
    throw error;
  }
};

export const exportTimesheet = async (userId: any, format: any= 'csv') => {
  try {
    const response = await API.get(`/users/${userId}/export/timesheet`, {
      params: { format },
      responseType: 'blob'
    });
    return response.data;
  } catch (error) {
    console.error('Export timesheet error:', error);
    throw error;
  }
};
