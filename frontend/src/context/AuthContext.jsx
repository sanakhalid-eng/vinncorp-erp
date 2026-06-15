import { useCallback, useEffect, useMemo, useState } from "react";
import { getProfile } from "../api/userApi";
import API from "../api/axios";
import { AuthContext } from "./authContext.js";

function parseJwt(token) {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(
    localStorage.getItem("accessToken") ||
      localStorage.getItem("token") ||
      null,
  );
  const [user, setUser] = useState(null);
  const [currentWorkspace, setCurrentWorkspace] = useState(() => {
    const saved = localStorage.getItem("activeWorkspaceId");
    return saved ? { id: Number(saved) } : null;
  });
  const [workspacePermissions, setWorkspacePermissions] = useState(() => {
    if (token) {
      const claims = parseJwt(token);
      return claims?.permissions || [];
    }
    return [];
  });
  const [workspaceRoles, setWorkspaceRoles] = useState(() => {
    if (token) {
      const claims = parseJwt(token);
      return claims?.workspaceRoles || [];
    }
    return [];
  });
  const [globalRoles, setGlobalRoles] = useState(() => {
    if (token) {
      const claims = parseJwt(token);
      return claims?.globalRoles || [];
    }
    return [];
  });
  const [workspaces, setWorkspaces] = useState([]);
  const [needsWorkspaceSelection, setNeedsWorkspaceSelection] =
    useState(false);

  const logout = useCallback(() => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userName");
    localStorage.removeItem("userId");
    localStorage.removeItem("activeWorkspaceId");
    localStorage.removeItem("activeWorkspaceSlug");
    setToken(null);
    setUser(null);
    setCurrentWorkspace(null);
    setWorkspacePermissions([]);
    setWorkspaceRoles([]);
    setGlobalRoles([]);
    setWorkspaces([]);
    setNeedsWorkspaceSelection(false);
  }, []);

  const updateTokenState = useCallback(
    (newToken) => {
      if (newToken) {
        localStorage.setItem("accessToken", newToken);
        localStorage.removeItem("token");
        setToken(newToken);

        const claims = parseJwt(newToken);
        if (claims) {
          setWorkspacePermissions(claims.permissions || []);
          setWorkspaceRoles(claims.workspaceRoles || []);
          setGlobalRoles(claims.globalRoles || []);

          if (claims.workspaceId) {
            setCurrentWorkspace({
              id: claims.workspaceId,
              slug: claims.workspaceSlug,
            });
            localStorage.setItem("activeWorkspaceId", claims.workspaceId);
            if (claims.workspaceSlug) {
              localStorage.setItem("activeWorkspaceSlug", claims.workspaceSlug);
            }
          }
        }
      }
    },
    [],
  );

  const selectWorkspace = useCallback(
    async (workspaceId, workspaceSlug) => {
      try {
        const res = await API.post("/auth/select-workspace", {
          workspaceId,
          workspaceSlug,
        });
        const data = res.data.data;
        if (data?.accessToken) {
          updateTokenState(data.accessToken);
          if (data.currentWorkspace) {
            setCurrentWorkspace(data.currentWorkspace);
          }
          if (data.workspaces) {
            setWorkspaces(data.workspaces);
          }
          return data;
        }
      } catch (err) {
        console.error("Failed to select workspace:", err);
        throw err;
      }
    },
    [updateTokenState],
  );

  const fetchProfile = useCallback(async () => {
    if (!token) return;
    try {
      const res = await getProfile();
      const profileData = res.data.data;
      if (profileData) {
        setUser((prev) => {
          if (
            prev &&
            prev.id === profileData.id &&
            prev.email === profileData.email
          ) {
            return prev;
          }
          return profileData;
        });
        if (profileData.name) {
          localStorage.setItem("userName", profileData.name);
        }
        if (profileData.id) {
          localStorage.setItem("userId", profileData.id);
        }
      }
    } catch (err) {
      console.error("Failed to fetch profile:", err);
      if (err.response?.status === 401) {
        logout();
      }
      setUser(null);
    }
  }, [logout, token]);

  useEffect(() => {
    if (token && !user) {
      fetchProfile();
    }
  }, [token]);

  // Listen for token refresh events from axios interceptor
  useEffect(() => {
    const handleTokenRefresh = (e) => {
      const newToken = e.detail?.token;
      if (newToken) {
        updateTokenState(newToken);
      }
    };
    window.addEventListener("token-refreshed", handleTokenRefresh);
    return () => window.removeEventListener("token-refreshed", handleTokenRefresh);
  }, [updateTokenState]);

  const login = useCallback(
    (newToken, loginData) => {
      updateTokenState(newToken);

      if (loginData?.workspaces && loginData.workspaces.length > 1) {
        setWorkspaces(loginData.workspaces);
        setNeedsWorkspaceSelection(true);
        setCurrentWorkspace(null);
      } else if (loginData?.currentWorkspace) {
        setCurrentWorkspace(loginData.currentWorkspace);
        setNeedsWorkspaceSelection(false);
      }
    },
    [updateTokenState],
  );

  const contextValue = useMemo(
    () => ({
      token,
      user,
      setUser,
      login,
      logout,
      fetchProfile,
      currentWorkspace,
      setCurrentWorkspace,
      workspacePermissions,
      workspaceRoles,
      globalRoles,
      workspaces,
      setWorkspaces,
      needsWorkspaceSelection,
      setNeedsWorkspaceSelection,
      selectWorkspace,
      updateTokenState,
    }),
    [
      token,
      user,
      login,
      logout,
      fetchProfile,
      currentWorkspace,
      workspacePermissions,
      workspaceRoles,
      globalRoles,
      workspaces,
      needsWorkspaceSelection,
      selectWorkspace,
      updateTokenState,
    ],
  );

  return (
    <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
  );
};
