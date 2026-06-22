import API from "./axios";

// Search & Knowledge
export const listSavedSearches = () => API.get("/saved-searches");
export const createSavedSearch = (body: any) => API.post("/saved-searches", body);
export const updateSavedSearch = (id: any, body: any) => API.put(`/saved-searches/${id}`, body);
export const deleteSavedSearch = (id: any) => API.delete(`/saved-searches/${id}`);

export const listKnowledgeArticles = (page: any= 0, size: any= 20) =>
  API.get("/knowledge", { params: { page, size } });
export const listPublishedArticles = (page: any= 0, size: any= 20) =>
  API.get("/knowledge/published", { params: { page, size } });
export const getKnowledgeBySlug = (slug: any) => API.get(`/knowledge/slug/${slug}`);
export const createKnowledgeArticle = (body: any) => API.post("/knowledge", body);
export const updateKnowledgeArticle = (id: any, body: any) => API.put(`/knowledge/${id}`, body);
export const deleteKnowledgeArticle = (id: any) => API.delete(`/knowledge/${id}`);

export const listWorkspaceNotes = (params: any= {}) => API.get("/notes", { params });
export const createWorkspaceNote = (body: any) => API.post("/notes", body);
export const updateWorkspaceNote = (id: any, body: any) => API.put(`/notes/${id}`, body);
export const deleteWorkspaceNote = (id: any) => API.delete(`/notes/${id}`);

export const getActivityIntelligence = (params: any= {}) =>
  API.get("/activity-intelligence/summary", { params });

// Executive Analytics
export const getExecutiveDashboard = () => API.get("/executive/dashboard");
export const captureExecutiveSnapshot = () => API.post("/executive/snapshots");
export const getExecutiveTrends = (page: any= 0, size: any= 10) =>
  API.get("/executive/trends", { params: { page, size } });
export const getDeliveryPredictability = (projectId: any) =>
  API.get(`/projects/${projectId}/delivery-predictability`);

// Productivity
export const getPersonalProductivity = () => API.get("/productivity/personal");
export const getNotificationIntelligence = () => API.get("/notifications/intelligence");
export const getQuickActions = () => API.get("/quick-actions");
export const getCalendarIntelligence = (projectId: any) =>
  API.get(`/projects/${projectId}/calendar-intelligence`);

export const searchCommandPalette = (q: any= "") =>
  API.get("/command-palette/search", { params: { q } });
export const getCommandPaletteRecent = () => API.get("/command-palette/recent");
export const recordCommandPaletteRecent = (body: any) =>
  API.post("/command-palette/recent", body);

export const bulkUpdateTasks = (body: any) => API.patch("/tasks/bulk", body);

// Sprint forecasting
export const getMonteCarloForecast = (sprintId: any) =>
  API.get(`/sprints/${sprintId}/monte-carlo`);
export const getCapacityForecast = (sprintId: any) =>
  API.get(`/sprints/${sprintId}/capacity-forecast`);
