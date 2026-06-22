import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import {
  getWorkspace,
  getWorkspaces,
  getWorkspaceBySlug,
} from "../features/settings/api/workspaceApi";
const WorkspaceContext = createContext(null);
export function WorkspaceProvider({ children }) {
  const [workspace, setWorkspace] = useState(null);
  const [workspaces, setWorkspaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [recentWorkspaces, setRecentWorkspaces] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("recentWorkspaces") || "[]");
    } catch {
      return [];
    }
  });
  const saveRecent = useCallback((ws) => {
    setRecentWorkspaces((prev) => {
      const filtered = prev.filter((w) => w.id !== ws.id);
      const updated = [ws, ...filtered].slice(0, 5);
      localStorage.setItem("recentWorkspaces", JSON.stringify(updated));
      return updated;
    });
  }, []);
  const switchWorkspace = useCallback(
    async (ws) => {
      localStorage.setItem("activeWorkspaceId", ws.id);
      localStorage.setItem("activeWorkspaceSlug", ws.slug);
      setWorkspace(ws);
      saveRecent(ws);
      window.dispatchEvent(
        new CustomEvent("workspace-changed", { detail: ws }),
      );
    },
    [saveRecent],
  );
  const loadWorkspaces = useCallback(async () => {
    try {
      const res = await getWorkspaces();
      const list = res.data.data || [];
      setWorkspaces(list);
      return list;
    } catch {
      return [];
    }
  }, []);
  const refreshWorkspace = useCallback(
    async (slug) => {
      setLoading(true);
      setError(null);
      try {
        const res = await getWorkspaceBySlug(slug);
        const ws = res.data.data;
        if (ws) {
          await switchWorkspace(ws);
        } else {
          setError("Workspace not found");
        }
      } catch (e) {
        setError("Failed to load workspace");
      } finally {
        setLoading(false);
      }
    },
    [switchWorkspace],
  );
  const value = useMemo(
    () => ({
      workspace,
      workspaces,
      loading,
      error,
      recentWorkspaces,
      switchWorkspace,
      refreshWorkspace,
      loadWorkspaces,
    }),
    [
      workspace,
      workspaces,
      loading,
      error,
      recentWorkspaces,
      switchWorkspace,
      refreshWorkspace,
      loadWorkspaces,
    ],
  );
  return (
    <WorkspaceContext.Provider value={value}>
       
      {children} 
    </WorkspaceContext.Provider>
  );
}
export function useWorkspace() {
  const ctx = useContext(WorkspaceContext);
  if (!ctx)
    throw new Error("useWorkspace must be used within WorkspaceProvider");
  return ctx;
}
