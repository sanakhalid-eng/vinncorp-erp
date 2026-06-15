import axios from "axios";
import { toast } from "sonner";

const API = axios.create({
  baseURL: import.meta.env.DEV ? "/api" : (import.meta.env.VITE_API_BASE_URL || "/api"),
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor
API.interceptors.request.use((req) => {
  const token = localStorage.getItem("accessToken");
  if (token && !req.url!.startsWith("/auth")) {
    req.headers.Authorization = `Bearer ${token}`;
  }

  // Add workspace context header (silently skip auth/public endpoints)
  if (!req.url!.startsWith("/auth") && !req.url!.startsWith("/workspaces") && !req.url!.startsWith("/workspace-invitations")) {
    const workspaceId = localStorage.getItem("activeWorkspaceId");
    if (workspaceId) {
      req.headers["X-Workspace-Id"] = workspaceId;
    }
  }

  if ((import.meta as Record<string, any>).env.DEV) {
    console.log(
      "API Request:",
      req.method?.toUpperCase(),
      req.url!,
      { auth: !!token, workspaceId: localStorage.getItem("activeWorkspaceId") }
    );
  }

  return req;
});

// Refresh handling state
let isRefreshing = false;
let refreshSubscribers: any[] = [];

// notify all pending requests
const onRefreshed = (newToken: any) => {
  refreshSubscribers.forEach((callback) => callback(newToken));
  refreshSubscribers = [];
};

const addSubscriber = (callback: any) => {
  refreshSubscribers.push(callback);
};

// 🔹 Response interceptor
API.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config;
    const status = err.response?.status;
    const message = err.response?.data?.message || err.response?.data?.error || "An error occurred";

    if ((import.meta as Record<string, any>).env.DEV) {
      console.error(
        "API Error:",
        err.message,
        status,
        originalRequest?.url
      );
    }

    // Handle 401 - Unauthorized (token expired)
    if (status === 401) {
      if (!originalRequest._retry) {
        originalRequest._retry = true;

        if (isRefreshing) {
          return new Promise((resolve) => {
            addSubscriber((newToken: any) => {
              originalRequest.headers.Authorization = `Bearer ${newToken}`;
              resolve(API(originalRequest));
            });
          });
        }

        isRefreshing = true;

        try {
          const refreshToken = localStorage.getItem("refreshToken");

          if (!refreshToken) throw new Error("No refresh token");

          // Use API instance so it goes through Vite proxy to backend
          const response = await API.post("/auth/refresh", {
            refreshToken,
          });

          // Handle different response structures
          const newAccessToken = response.data?.data?.accessToken || response.data?.data?.token || response.data?.accessToken || response.data?.token;
          const newRefreshToken = response.data?.data?.refreshToken || response.data?.refreshToken;

          if (!newAccessToken) {
            throw new Error("No access token in refresh response");
          }

          localStorage.setItem("accessToken", newAccessToken);

          // Store rotated refresh token
          if (newRefreshToken) {
            localStorage.setItem("refreshToken", newRefreshToken);
          }

          onRefreshed(newAccessToken);

          // Notify AuthContext of new token so it can re-parse permissions
          window.dispatchEvent(
            new CustomEvent("token-refreshed", { detail: { token: newAccessToken } })
          );

          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

          return API(originalRequest);

        } catch (refreshError) {
          console.error("Refresh failed → logging out");

          localStorage.clear();

          if (typeof window !== "undefined") {
            window.location.href = "/login";
          }

          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      }
    }

    // Handle 403 - Forbidden/Access Denied
    if (status === 403) {
      toast.error("Access Denied: You don't have permission to perform this action");
    }

    // Handle 400 - Bad Request
    if (status === 400) {
      toast.error(message || "Invalid request");
    }

    // Handle 404 - Not Found
    if (status === 404) {
      toast.error("Resource not found");
    }

    // Handle 500 - Server Error
    if (status === 500) {
      toast.error("Server error. Please try again later.");
    }

    return Promise.reject(err);
  }
);

export default API;