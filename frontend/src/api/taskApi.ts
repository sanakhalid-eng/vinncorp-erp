import API from './axios';
import { getProjectMembers } from './projectMembersApi.js';

/**
 * Task Management API - Matches backend TaskController exactly
 * Tasks are PROJECT-SCOPED - projectId required for create/list
 */

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

const normalizeTask = (task: any) => ({
  ...task,
  statusId: task?.statusId ?? null,
  status: task?.status ?? 'UNKNOWN',
  priority: task?.priority ?? 'MEDIUM',
  project: task?.project ?? null,
  assignee: task?.assignee ?? null,
  createdBy: task?.createdBy ?? null,
});

const normalizePage = (pageData: any) => {
  const content = Array.isArray(pageData?.content) ? pageData.content.map(normalizeTask) : [];

  return {
    content,
    page: pageData?.page ?? 0,
    size: pageData?.size ?? content.length,
    totalElements: pageData?.totalElements ?? content.length,
    totalPages: pageData?.totalPages ?? (content.length > 0 ? 1 : 0),
    last: pageData?.last ?? true,
  };
};


// Get tasks for specific project with full filtering/pagination
export const getTasksByProject = async (projectId: any, {
  page = 0,
  size = 10,
  statusId,
  priority,
  search,
  assigneeId,
  startDate,
  endDate,
  sortBy = 'createdAt',
  sortDir = 'desc'
}: any = {}) => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sortBy,
    sortDir,
    ...(statusId != null && statusId !== '' ? { statusId: statusId.toString() } : {}),
    ...(priority && priority !== 'all' ? { priority } : {}),
    ...(search ? { search } : {}),
    ...(assigneeId ? { assigneeId: assigneeId.toString() } : {}),
    ...(startDate ? { startDate } : {}),
    ...(endDate ? { endDate } : {})
  });

  try {
    const res = await API.get(`/tasks/project/${projectId}?${params}`);
    return normalizePage(unwrapData(res));
  } catch (error) {
    console.error('Get project tasks error:', error);
    throw error;
  }
};

// Get single task
export const getTaskById = async (id: any) => {
  try {
    const res = await API.get(`/tasks/${id}`);
    return normalizeTask(unwrapData(res));
  } catch (error) {
    console.error('Get task error:', error);
    throw error;
  }
};

// Create task (requires projectId)
export const createTask = async (taskData: any) => {
  try {
    const res = await API.post('/tasks', taskData);
    return normalizeTask(unwrapData(res));
  } catch (error) {
    console.error('Create task error:', error);
    throw error;
  }
};

// Update task
export const updateTask = async (id: any, taskData: any) => {
  try {
    const res = await API.put(`/tasks/${id}`, taskData);
    return normalizeTask(unwrapData(res));
  } catch (error) {
    console.error('Update task error:', error);
    throw error;
  }
};

// Update task status only (PATCH)
export const updateTaskStatus = async (id: any, statusId: any) => {
  try {
    const res = await API.patch(`/tasks/${id}/status?statusId=${statusId}`);
    return normalizeTask(unwrapData(res));
  } catch (error) {
    console.error('Update task status error:', error);
    throw error;
  }
};

// Delete task
export const deleteTask = async (id: any) => {
  try {
    await API.delete(`/tasks/${id}`);
  } catch (error) {
    console.error('Delete task error:', error);
    throw error;
  }
};

// My tasks (paginated)
export const getMyTasks = async (page: any= 0, size: any= 10) => {
  try {
    const res = await API.get(`/tasks/my-tasks?page=${page}&size=${size}`);
    return normalizePage(unwrapData(res));
  } catch (error) {
    console.error('Get my tasks error:', error);
    throw error;
  }
};

// Get project members for assignee dropdown
export const getProjectAssignees = (projectId: any) => getProjectMembers(projectId);

// Clone (duplicate) a task
export const cloneTask = async (taskId: any) => {
  try {
    const res = await API.post(`/tasks/${taskId}/clone`);
    return normalizeTask(unwrapData(res));
  } catch (error) {
    console.error('Clone task error:', error);
    throw error;
  }
};

// Move task between columns
export const moveTask = async (taskId: any, sourceColumnId: any, targetColumnId: any, position: any) => {
  try {
    const res = await API.patch('/tasks/move', {
      taskId,
      sourceColumnId,
      targetColumnId,
      position,
    });
    return normalizeTask(unwrapData(res));
  } catch (error) {
    console.error('Move task error:', error);
    throw error;
  }
};

// Export tasks as PDF
export const exportTasksPdf = async (projectId: any) => {
  try {
    const res = await API.get(`/tasks/project/${projectId}/export/pdf`, { responseType: 'blob' });
    return res.data;
  } catch (error) {
    console.error('Export tasks PDF error:', error);
    throw error;
  }
};

// Bulk update tasks
export const bulkUpdateTasks = async (taskIds: any[], updateData: any) => {
  try {
    const res = await API.patch('/tasks/bulk', { taskIds, ...updateData });
    return res.data;
  } catch (error) {
    console.error('Bulk update tasks error:', error);
    throw error;
  }
};

// Bulk delete tasks
export const bulkDeleteTasks = async (taskIds: any[]) => {
  try {
    const res = await API.delete('/tasks/bulk', { data: { taskIds } });
    return res.data;
  } catch (error) {
    console.error('Bulk delete tasks error:', error);
    throw error;
  }
};

