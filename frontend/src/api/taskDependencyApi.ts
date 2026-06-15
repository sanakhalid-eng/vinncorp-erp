import API from './axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

const normalizeDependency = (dep: any) => ({
  id: dep?.id ?? null,
  taskId: dep?.taskId ?? null,
  taskTitle: dep?.taskTitle ?? '',
  dependsOnTaskId: dep?.dependsOnTaskId ?? null,
  dependsOnTaskTitle: dep?.dependsOnTaskTitle ?? '',
  dependsOnTaskStatus: dep?.dependsOnTaskStatus ?? '',
  dependsOnTaskCompleted: dep?.dependsOnTaskCompleted ?? false,
  dependencyType: dep?.dependencyType ?? 'BLOCKED_BY',
  description: dep?.description ?? '',
  createdAt: dep?.createdAt ?? null,
});

export const addTaskDependency = async (taskId: any, dependsOnTaskId: any, dependencyType: any= 'BLOCKED_BY', description: any= '') => {
  try {
    const res = await API.post(`/tasks/${taskId}/dependencies`, {
      dependsOnTaskId,
      dependencyType,
      description,
    });
    return normalizeDependency(unwrapData(res));
  } catch (error) {
    console.error('Add task dependency error:', error);
    throw error;
  }
};

export const removeTaskDependency = async (taskId: any, dependsOnTaskId: any) => {
  try {
    await API.delete(`/tasks/${taskId}/dependencies?dependsOnTaskId=${dependsOnTaskId}`);
  } catch (error) {
    console.error('Remove task dependency error:', error);
    throw error;
  }
};

export const getTaskDependencies = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/dependencies`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data.map(normalizeDependency) : [];
  } catch (error) {
    console.error('Get task dependencies error:', error);
    throw error;
  }
};

export const getBlockingTasks = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/blocking-tasks`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data.map(normalizeDependency) : [];
  } catch (error) {
    console.error('Get blocking tasks error:', error);
    throw error;
  }
};

export const getTaskDependencyGraph = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/dependency-graph`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get dependency graph error:', error);
    throw error;
  }
};

export const getTaskBlockedStatus = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/blocked-status`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get blocked status error:', error);
    throw error;
  }
};

export const getRelatedTasks = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/related`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data.map(normalizeDependency) : [];
  } catch (error) {
    console.error('Get related tasks error:', error);
    throw error;
  }
};
