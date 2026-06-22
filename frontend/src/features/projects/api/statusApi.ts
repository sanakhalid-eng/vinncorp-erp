import API from '../../../api/axios.js';
import { toast } from 'sonner';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? null;

export const getProjectStatuses = async (projectId: any) => {
  try {
    const res = await API.get(`/projects/${projectId}/workflow/statuses`);
    const data = unwrapData(res) || [];

    return Array.isArray(data)
      ? [...data].sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0))
      : [];
  } catch (error: any) {
    console.error('Get project statuses error:', error);
    toast.error(error.response?.data?.message || 'Failed to load statuses');
    return [];
  }
};

export const createStatus = async (projectId: any, statusData: any) => {
  try {
    const res = await API.post(`/projects/${projectId}/workflow/statuses`, statusData);
    return unwrapData(res);
  } catch (error: any) {
    console.error('Create status error:', error);
    toast.error(error.response?.data?.message || 'Failed to create status');
    throw error;
  }
};

export const updateStatus = async (projectId: any, statusId: any, statusData: any) => {
  try {
    const res = await API.patch(`/projects/${projectId}/workflow/statuses/${statusId}`, statusData);
    return unwrapData(res);
  } catch (error: any) {
    console.error('Update status error:', error);
    toast.error(error.response?.data?.message || 'Failed to update status');
    throw error;
  }
};

export const deleteStatus = async (projectId: any, statusId: any) => {
  try {
    await API.delete(`/projects/${projectId}/workflow/statuses/${statusId}`);
  } catch (error: any) {
    console.error('Delete status error:', error);
    toast.error(error.response?.data?.message || 'Failed to delete status');
    throw error;
  }
};

export const reorderStatuses = async (projectId: any, statuses: any) => {
  try {
    const payload = statuses.map((status: any, index: any) => ({
      id: status.id,
      orderIndex: index,
    }));
    const res = await API.patch(`/projects/${projectId}/workflow/statuses/reorder`, payload);
    return unwrapData(res) || [];
  } catch (error: any) {
    console.error('Reorder statuses error:', error);
    toast.error(error.response?.data?.message || 'Failed to reorder statuses');
    throw error;
  }
};

export const getProjectTransitions = async (projectId: any) => {
  try {
    const res = await API.get(`/projects/${projectId}/workflow/transitions`);
    const data = unwrapData(res) || [];
    return Array.isArray(data) ? data : [];
  } catch (error: any) {
    console.error('Get project transitions error:', error);
    toast.error(error.response?.data?.message || 'Failed to load transitions');
    return [];
  }
};

export const createTransition = async (projectId: any, transitionData: any) => {
  try {
    const res = await API.post(`/projects/${projectId}/workflow/transitions`, {
      fromStatusId: Number(transitionData.fromStatusId),
      toStatusId: Number(transitionData.toStatusId),
    });
    return unwrapData(res);
  } catch (error: any) {
    console.error('Create transition error:', error);
    toast.error(error.response?.data?.message || 'Failed to create transition');
    throw error;
  }
};

export const deleteTransition = async (projectId: any, transitionId: any) => {
  try {
    await API.delete(`/projects/${projectId}/workflow/transitions/${transitionId}`);
  } catch (error: any) {
    console.error('Delete transition error:', error);
    toast.error(error.response?.data?.message || 'Failed to delete transition');
    throw error;
  }
};

export const validateTransition = async (projectId: any, fromStatusId: any, toStatusId: any) => {
  const normalizedFrom = Number(fromStatusId);
  const normalizedTo = Number(toStatusId);

  if (!normalizedFrom || !normalizedTo) {
    return { valid: true, message: '' };
  }

  const statuses = await getProjectStatuses(projectId);
  const transitions = await getProjectTransitions(projectId);

  const fromStatus = statuses.find((status) => Number(status.id) === normalizedFrom);
  const toStatus = statuses.find((status) => Number(status.id) === normalizedTo);

  if (normalizedFrom === normalizedTo) {
    return { valid: false, message: 'Cannot transition to same status' };
  }

  if (!fromStatus || !toStatus) {
    return { valid: false, message: 'Selected statuses are invalid' };
  }

  if ((fromStatus.orderIndex ?? 0) >= (toStatus.orderIndex ?? 0)) {
    return { valid: false, message: 'Backflow not allowed' };
  }

  const duplicate = transitions.some(
    (transition) =>
      Number(transition.fromStatusId) === normalizedFrom &&
      Number(transition.toStatusId) === normalizedTo
  );

  if (duplicate) {
    return { valid: false, message: 'This transition already exists' };
  }

  return { valid: true, message: '' };
};
