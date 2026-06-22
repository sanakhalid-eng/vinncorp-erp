import API from '../../../api/axios';

export async function createSubtask(taskId: any, data: any) {
  const res = await API.post(`/tasks/${taskId}/subtasks`, data);
  return res.data;
}

export async function getSubtasks(taskId: any) {
  const res = await API.get(`/tasks/${taskId}/subtasks`);
  return res.data;
}

export async function getSubtaskProgress(taskId: any) {
  const res = await API.get(`/tasks/${taskId}/subtasks/progress`);
  return res.data;
}

export async function updateSubtaskParent(taskId: any, parentTaskId: any) {
  const res = await API.patch(`/tasks/${taskId}/parent`, { parentTaskId });
  return res.data;
}

export async function toggleSubtaskCompletion(subtaskId: any) {
  const res = await API.patch(`/tasks/${subtaskId}/toggle-completion`);
  return res.data;
}

export async function updateSubtask(taskId: any, subtaskId: any, data: any) {
  const res = await API.put(`/tasks/${taskId}/subtasks/${subtaskId}`, data);
  return res.data;
}

export async function deleteSubtask(taskId: any, subtaskId: any) {
  const res = await API.delete(`/tasks/${taskId}/subtasks/${subtaskId}`);
  return res.data;
}
