import API from '../../../api/axios';

export const getProjectTemplates = async () => {
  const res = await API.get('/templates');
  return res.data?.data ?? [];
};

export const saveProjectAsTemplate = async (projectId: any, templateData: any) => {
  const res = await API.post(`/projects/${projectId}/save-template`, templateData);
  return res.data?.data ?? res.data;
};

export const createProjectFromTemplate = async (templateId: any, projectData: any) => {
  const res = await API.post(`/templates/${templateId}/create-project`, projectData);
  return res.data?.data ?? res.data;
};
