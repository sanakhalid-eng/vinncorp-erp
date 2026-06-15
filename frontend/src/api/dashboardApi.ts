import API from "./axios";

export const getDashboardStats = async (options: any= {}) => {
  const response = await API.get("/dashboard/summary", options);
  return response.data?.data ?? {};
};
