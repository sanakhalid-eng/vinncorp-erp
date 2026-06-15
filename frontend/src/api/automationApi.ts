import API from './axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

export const createRule = async (data: any) => {
  try {
    const res = await API.post('/workflow-rules', data);
    return unwrapData(res);
  } catch (error) {
    console.error('Create rule error:', error);
    throw error;
  }
};

export const updateRule = async (ruleId: any, data: any) => {
  try {
    const res = await API.put(`/workflow-rules/${ruleId}`, data);
    return unwrapData(res);
  } catch (error) {
    console.error('Update rule error:', error);
    throw error;
  }
};

export const getRule = async (ruleId: any) => {
  try {
    const res = await API.get(`/workflow-rules/${ruleId}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get rule error:', error);
    throw error;
  }
};

export const getWorkspaceRules = async (workspaceId: any) => {
  try {
    const res = await API.get(`/workflow-rules/workspace/${workspaceId}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get workspace rules error:', error);
    throw error;
  }
};

export const getProjectRules = async (workspaceId: any, projectId: any) => {
  try {
    const res = await API.get(`/workflow-rules/workspace/${workspaceId}/project/${projectId}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get project rules error:', error);
    throw error;
  }
};

export const deleteRule = async (ruleId: any) => {
  try {
    await API.delete(`/workflow-rules/${ruleId}`);
  } catch (error) {
    console.error('Delete rule error:', error);
    throw error;
  }
};

export const toggleRule = async (ruleId: any, enabled: any) => {
  try {
    await API.patch(`/workflow-rules/${ruleId}/toggle?enabled=${enabled}`);
  } catch (error) {
    console.error('Toggle rule error:', error);
    throw error;
  }
};

export const getExecutionLogs = async (ruleId: any, page: any= 0, size: any= 20) => {
  try {
    const res = await API.get(`/workflow-rules/${ruleId}/logs?page=${page}&size=${size}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get execution logs error:', error);
    throw error;
  }
};

export const getRecentLogs = async (workspaceId: any) => {
  try {
    const res = await API.get(`/workflow-rules/logs/recent?workspaceId=${workspaceId}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get recent logs error:', error);
    throw error;
  }
};

export const getTemplates = async () => {
  try {
    const res = await API.get('/workflow-rules/templates');
    return unwrapData(res);
  } catch (error) {
    console.error('Get templates error:', error);
    throw error;
  }
};

export const applyTemplate = async (templateKey: any, workspaceId: any, projectId: any) => {
  try {
    const res = await API.post(`/workflow-rules/templates/${templateKey}/apply?workspaceId=${workspaceId}&projectId=${projectId || ''}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Apply template error:', error);
    throw error;
  }
};

export const autoAssignTask = async (taskId: any) => {
  try {
    const res = await API.post(`/automation/tasks/${taskId}/auto-assign`);
    return unwrapData(res);
  } catch (error) {
    console.error('Auto assign error:', error);
    throw error;
  }
};

export const configureSLA = async (taskId: any, data: any) => {
  try {
    const res = await API.post(`/tasks/${taskId}/sla`, data);
    return unwrapData(res);
  } catch (error) {
    console.error('Configure SLA error:', error);
    throw error;
  }
};

export const getTaskSLA = async (taskId: any, slaType: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/sla?slaType=${slaType}`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get task SLA error:', error);
    throw error;
  }
};

export const getSLAReport = async (projectId: any) => {
  try {
    const res = await API.get(`/projects/${projectId}/sla-report`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get SLA report error:', error);
    throw error;
  }
};
