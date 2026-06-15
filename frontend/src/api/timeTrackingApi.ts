import API from './axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

export const logTime = async (taskId: any, userId: any, timeLogRequest: any) => {
  try {
    const res = await API.post(`/tasks/${taskId}/time-logs?userId=${userId}`, timeLogRequest);
    return unwrapData(res);
  } catch (error) {
    console.error('Log time error:', error);
    throw error;
  }
};

export const getTaskTimeLogs = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/time-logs`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error('Get time logs error:', error);
    throw error;
  }
};

export const updateTimeLog = async (logId: any, userId: any, timeLogRequest: any) => {
  try {
    const res = await API.put(`/time-logs/${logId}?userId=${userId}`, timeLogRequest);
    return unwrapData(res);
  } catch (error) {
    console.error('Update time log error:', error);
    throw error;
  }
};

export const deleteTimeLog = async (logId: any, userId: any, isAdmin: any= false) => {
  try {
    await API.delete(`/time-logs/${logId}?userId=${userId}&isAdmin=${isAdmin}`);
  } catch (error) {
    console.error('Delete time log error:', error);
    throw error;
  }
};

export const getUserTimesheet = async (userId: any, range: any= 'weekly') => {
  try {
    const res = await API.get(`/users/${userId}/timesheet?range=${range}`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error('Get timesheet error:', error);
    throw error;
  }
};

export const getTaskTimeSummary = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/time-summary`);
    return unwrapData(res) || 0;
  } catch (error) {
    console.error('Get task time summary error:', error);
    throw error;
  }
};

export const startTimer = async (taskId: any, userId: any, description: any= null) => {
  try {
    const res = await API.post(`/tasks/${taskId}/timer/start?userId=${userId}`, description ? { description } : {});
    return unwrapData(res);
  } catch (error) {
    console.error('Start timer error:', error);
    throw error;
  }
};

export const stopTimer = async (userId: any, description: any= null) => {
  try {
    const res = await API.post(`/timer/stop?userId=${userId}`, description ? { description } : {});
    return unwrapData(res);
  } catch (error) {
    console.error('Stop timer error:', error);
    throw error;
  }
};

export const getActiveTimer = async (userId: any) => {
  try {
    const res = await API.get(`/timer/active?userId=${userId}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get active timer error:', error);
    throw error;
  }
};

export const hasActiveTimer = async (userId: any) => {
  try {
    const res = await API.get(`/timer/has-active?userId=${userId}`);
    return unwrapData(res) || false;
  } catch (error) {
    console.error('Check active timer error:', error);
    throw error;
  }
};

// Timesheet Approval APIs
export const submitTimesheet = async (userId: any, weekStart: any) => {
  try {
    const res = await API.post(`/timesheets/submit?userId=${userId}&weekStart=${weekStart}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Submit timesheet error:', error);
    throw error;
  }
};

export const approveTimesheet = async (approvalId: any, approverId: any) => {
  try {
    const res = await API.post(`/timesheets/${approvalId}/approve?approverId=${approverId}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Approve timesheet error:', error);
    throw error;
  }
};

export const rejectTimesheet = async (approvalId: any, approverId: any, reason: any= null) => {
  try {
    const params = reason ? `?approverId=${approverId}&reason=${encodeURIComponent(reason)}` : `?approverId=${approverId}`;
    const res = await API.post(`/timesheets/${approvalId}/reject${params}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Reject timesheet error:', error);
    throw error;
  }
};

export const getPendingTimesheets = async () => {
  try {
    const res = await API.get('/timesheets/pending');
    return unwrapData(res) || [];
  } catch (error) {
    console.error('Get pending timesheets error:', error);
    throw error;
  }
};

export const getUserTimesheets = async (userId: any) => {
  try {
    const res = await API.get(`/users/${userId}/timesheets`);
    return unwrapData(res) || [];
  } catch (error) {
    console.error('Get user timesheets error:', error);
    throw error;
  }
};
