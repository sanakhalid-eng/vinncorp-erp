import API from '../../../api/axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

const normalizeTemplate = (t: any) => ({
  ...t,
  daysOfWeek: t?.daysOfWeek ?? [],
});

const normalizeOccurrence = (o: any) => ({
  ...o,
  occurrenceDate: o?.occurrenceDate ?? null,
  generationStatus: o?.generationStatus ?? 'GENERATED',
});

export const createRecurring = async (taskId: any, data: any) => {
  try {
    const res = await API.post(`/tasks/${taskId}/recurring`, data);
    return normalizeTemplate(unwrapData(res));
  } catch (error) {
    console.error('Create recurring error:', error);
    throw error;
  }
};

export const updateRecurring = async (id: any, data: any) => {
  try {
    const res = await API.put(`/recurring/${id}`, data);
    return normalizeTemplate(unwrapData(res));
  } catch (error) {
    console.error('Update recurring error:', error);
    throw error;
  }
};

export const getRecurringTemplate = async (id: any) => {
  try {
    const res = await API.get(`/recurring/${id}`);
    return normalizeTemplate(unwrapData(res));
  } catch (error) {
    console.error('Get recurring template error:', error);
    throw error;
  }
};

export const getProjectRecurringTemplates = async (projectId: any) => {
  try {
    const res = await API.get(`/recurring/project/${projectId}`);
    const templates = unwrapData(res);
    return Array.isArray(templates) ? templates.map(normalizeTemplate) : [];
  } catch (error) {
    console.error('Get project recurring templates error:', error);
    throw error;
  }
};

export const pauseRecurring = async (id: any) => {
  try {
    const res = await API.post(`/recurring/${id}/pause`);
    return normalizeTemplate(unwrapData(res));
  } catch (error) {
    console.error('Pause recurring error:', error);
    throw error;
  }
};

export const resumeRecurring = async (id: any) => {
  try {
    const res = await API.post(`/recurring/${id}/resume`);
    return normalizeTemplate(unwrapData(res));
  } catch (error) {
    console.error('Resume recurring error:', error);
    throw error;
  }
};

export const stopRecurring = async (id: any) => {
  try {
    await API.post(`/recurring/${id}/stop`);
  } catch (error) {
    console.error('Stop recurring error:', error);
    throw error;
  }
};

export const getOccurrences = async (templateId: any) => {
  try {
    const res = await API.get(`/recurring/${templateId}/occurrences`);
    const occurrences = unwrapData(res);
    return Array.isArray(occurrences) ? occurrences.map(normalizeOccurrence) : [];
  } catch (error) {
    console.error('Get occurrences error:', error);
    throw error;
  }
};
