import API from "./axios";

export const getWorkspaces = () => API.get("/workspaces");

export const getWorkspace = (id: any) => API.get(`/workspaces/${id}`);

export const createWorkspace = (data: any) => API.post("/workspaces", data);

export const updateWorkspace = (id: any, data: any) => API.put(`/workspaces/${id}`, data);

export const deleteWorkspace = (id: any) => API.delete(`/workspaces/${id}`);

export const getWorkspaceMembers = (id: any) => API.get(`/workspaces/${id}/members`);

export const removeWorkspaceMember = (workspaceId: any, userId: any) =>
  API.delete(`/workspaces/${workspaceId}/members/${userId}`);

export const getWorkspaceSettings = (id: any) => API.get(`/workspaces/${id}/settings`);

export const switchWorkspace = (id: any) => API.post(`/workspaces/${id}/switch`);

export const getWorkspaceInvitations = (id: any) => API.get(`/workspaces/${id}/invitations`);

export const createWorkspaceInvitation = (id: any, data: any) =>
  API.post(`/workspaces/${id}/invitations`, data);

export const revokeWorkspaceInvitation = (id: any) =>
  API.delete(`/workspace-invitations/${id}`);

export const acceptWorkspaceInvitation = (token: any) =>
  API.post(`/workspace-invitations/accept/${token}`);

export const getWorkspaceInvitationByToken = (token: any) =>
  API.get(`/workspace-invitations/${token}`);

export const getWorkspaceBySlug = (slug: any) => API.get(`/workspaces/slug/${slug}`);

export const getWorkspacePreferences = (id: any) => API.get(`/workspaces/${id}/preferences`);

export const updateWorkspacePreferences = (id: any, data: any) => API.put(`/workspaces/${id}/preferences`, data);

export const getProjectTemplates = () => API.get("/templates");

export const uploadWorkspaceLogo = (id: any, file: any) => {
  const formData = new FormData();
  formData.append("file", file);
  return API.post(`/workspaces/${id}/logo`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};
