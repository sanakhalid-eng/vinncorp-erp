import API from '../../../api/axios';

export async function createLabel(projectId: any, data: any) {
  const res = await API.post(`/projects/${projectId}/labels`, data);
  return res.data;
}

export async function getProjectLabels(projectId: any) {
  const res = await API.get(`/projects/${projectId}/labels`);
  return res.data;
}

export async function deleteLabel(labelId: any) {
  const res = await API.delete(`/labels/${labelId}`);
  return res.data;
}

export async function assignLabelsToTask(taskId: any, labelIds: any) {
  const res = await API.post(`/tasks/${taskId}/labels`, { labelIds });
  return res.data;
}

export async function removeLabelFromTask(taskId: any, labelId: any) {
  const res = await API.delete(`/tasks/${taskId}/labels/${labelId}`);
  return res.data;
}

export async function getTaskLabels(taskId: any) {
  const res = await API.get(`/tasks/${taskId}/labels`);
  return res.data;
}

export async function bulkRemoveLabelsFromTask(taskId: any, labelIds: any) {
  const res = await API.patch(`/tasks/${taskId}/labels/bulk-remove`, { labelIds });
  return res.data;
}
