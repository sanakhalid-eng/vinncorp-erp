import API from "../../../api/axios";

export const getSystemSettings = () => API.get("/system/settings");

export const transferOwnership = (targetUserId: any) =>
  API.post("/system/transfer-ownership", { targetUserId });

export const getSystemHealth = () => API.get("/system/health");

export const getJobStatuses = () => API.get("/system/jobs");
