import API from "./axios";

export const createInvitation = (projectId: any, data: any) =>
  API.post(`/projects/${projectId}/invitations`, data);

export const getProjectInvitations = (projectId: any) =>
  API.get(`/projects/${projectId}/invitations`);

export const revokeInvitation = (id: any) =>
  API.delete(`/invitations/${id}`);

export const acceptInvitation = (token: any) =>
  API.post(`/invitations/accept/${token}`);

export const getInvitation = (token: any) =>
  API.get(`/invitations/${token}`);
