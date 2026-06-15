import API from './axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

const normalizeSprint = (sprint: any) => ({
  id: sprint?.id ?? null,
  projectId: sprint?.projectId ?? null,
  projectName: sprint?.projectName ?? '',
  name: sprint?.name ?? '',
  goal: sprint?.goal ?? '',
  startDate: sprint?.startDate ?? null,
  endDate: sprint?.endDate ?? null,
  status: sprint?.status ?? 'PLANNED',
  totalTasks: sprint?.totalTasks ?? 0,
  completedTasks: sprint?.completedTasks ?? 0,
  progressPercentage: sprint?.progressPercentage ?? 0,
  summaryTotalTasks: sprint?.summaryTotalTasks ?? null,
  summaryCompletedTasks: sprint?.summaryCompletedTasks ?? null,
  summaryCarriedForward: sprint?.summaryCarriedForward ?? null,
  completedAt: sprint?.completedAt ?? null,
  createdAt: sprint?.createdAt ?? null,
  updatedAt: sprint?.updatedAt ?? null,
});

export const createSprint = async (sprintData: any) => {
  try {
    const res = await API.post('/sprints', sprintData);
    return normalizeSprint(unwrapData(res));
  } catch (error) {
    console.error('Create sprint error:', error);
    throw error;
  }
};

export const startSprint = async (sprintId: any) => {
  try {
    const res = await API.post(`/sprints/${sprintId}/start`);
    return normalizeSprint(unwrapData(res));
  } catch (error) {
    console.error('Start sprint error:', error);
    throw error;
  }
};

export const completeSprint = async (sprintId: any, carryForward: any= false) => {
  try {
    const res = await API.post(`/sprints/${sprintId}/complete?carryForward=${carryForward}`);
    return normalizeSprint(unwrapData(res));
  } catch (error) {
    console.error('Complete sprint error:', error);
    throw error;
  }
};

export const getProjectSprints = async (projectId: any) => {
  try {
    const res = await API.get(`/sprints/project/${projectId}`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data.map(normalizeSprint) : [];
  } catch (error) {
    console.error('Get project sprints error:', error);
    throw error;
  }
};

export const getSprints = async (projectId: any) => {
  return getProjectSprints(projectId);
};

export const getActiveSprint = async (projectId: any) => {
  try {
    const res = await API.get(`/sprints/project/${projectId}/active`);
    return normalizeSprint(unwrapData(res));
  } catch (error) {
    console.error('Get active sprint error:', error);
    throw error;
  }
};

export const getSprintById = async (sprintId: any) => {
  try {
    const res = await API.get(`/sprints/${sprintId}`);
    return normalizeSprint(unwrapData(res));
  } catch (error) {
    console.error('Get sprint error:', error);
    throw error;
  }
};

export const getBacklogTasks = async (projectId: any) => {
  try {
    const res = await API.get(`/sprints/project/${projectId}/backlog`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error('Get backlog tasks error:', error);
    throw error;
  }
};

export const deleteSprint = async (sprintId: any) => {
  try {
    await API.delete(`/sprints/${sprintId}`);
  } catch (error) {
    console.error('Delete sprint error:', error);
    throw error;
  }
};

export const assignTaskToSprint = async (taskId: any, sprintId: any) => {
  try {
    await API.post(`/tasks/${taskId}/sprint/${sprintId}`);
  } catch (error) {
    console.error('Assign task to sprint error:', error);
    throw error;
  }
};

export const removeTaskFromSprint = async (taskId: any) => {
  try {
    await API.delete(`/tasks/${taskId}/sprint`);
  } catch (error) {
    console.error('Remove task from sprint error:', error);
    throw error;
  }
};

export const getSprintTasks = async (sprintId: any) => {
  try {
    const res = await API.get(`/tasks/sprint/${sprintId}/tasks`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error('Get sprint tasks error:', error);
    throw error;
  }
};

export const getSprintBurndown = async (sprintId: any) => {
  try {
    const res = await API.get(`/sprints/${sprintId}/burndown`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error('Get sprint burndown error:', error);
    throw error;
  }
};

export const getProjectVelocityHistory = async (projectId: any) => {
  try {
    const res = await API.get(`/projects/${projectId}/velocity-history`);
    const data = unwrapData(res);
    return data;
  } catch (error) {
    console.error('Get project velocity history error:', error);
    throw error;
  }
};
