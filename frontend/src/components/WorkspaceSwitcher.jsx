import { useState, useEffect, useRef, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import {
  Check,
  ChevronDown,
  Plus,
  Building2,
  Loader2,
  Search,
  Clock,
} from "lucide-react";
import { getWorkspaces } from "../api/workspaceApi";
import { useWorkspace } from "../context/WorkspaceContext";
import { useAuth } from "../context/useAuth.js";

const WS_COLORS = [
  "from-cyan-400 to-indigo-500",
  "from-pink-400 to-rose-500",
  "from-emerald-400 to-teal-500",
  "from-amber-400 to-orange-500",
  "from-violet-400 to-purple-500",
  "from-sky-400 to-blue-500",
  "from-fuchsia-400 to-pink-500",
  "from-lime-400 to-green-500",
];
function getColor(index) {
  return WS_COLORS[index % WS_COLORS.length];
}
export default function WorkspaceSwitcher({ collapsed }) {
  const navigate = useNavigate();
  const {
    workspace: ctxWorkspace,
    workspaces,
    loadWorkspaces,
    recentWorkspaces,
    switchWorkspace,
  } = useWorkspace();
  const { selectWorkspace } = useAuth();
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [switching, setSwitching] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [optimisticWs, setOptimisticWs] = useState(null);
  const searchInputRef = useRef(null);
  const ref = useRef(null);
  const activeWorkspace = optimisticWs || ctxWorkspace;
  useEffect(() => {
    const init = async () => {
      if (workspaces.length === 0) await loadWorkspaces();
      setLoading(false);
    };
    init();
  }, []);
  useEffect(() => {
    if (open && searchInputRef.current) {
      setTimeout(() => searchInputRef.current?.focus(), 50);
      setSearchQuery("");
    }
  }, [open]);
  useEffect(() => {
    const handleClick = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);
  const filteredWorkspaces = useMemo(() => {
    if (!searchQuery.trim()) return workspaces;
    const q = searchQuery.toLowerCase();
    return workspaces.filter(
      (w) =>
        w.name?.toLowerCase().includes(q) || w.slug?.toLowerCase().includes(q),
    );
  }, [workspaces, searchQuery]);
  const handleSelect = async (ws) => {
    if (ws.id === activeWorkspace?.id) return;
    setOptimisticWs(ws);
    setOpen(false);
    setSwitching(true);
    try {
      await selectWorkspace(ws.id);
      await switchWorkspace(ws);
      window.dispatchEvent(
        new CustomEvent("workspace-changed", { detail: ws }),
      );
    } catch (error) {
      console.error("Failed to switch workspace:", error);
      setOptimisticWs(null);
    } finally {
      setSwitching(false);
    }
  };
  if (loading || switching) {
    return (
      <div
        className={`flex items-center ${collapsed ? "justify-center" : "gap-3"} px-3 py-3`}
      >
        <Loader2 className="h-5 w-5 animate-spin text-slate-400" />
        {!collapsed && switching && (
          <span className="text-xs text-slate-400">Switching...</span>
        )}
      </div>
    );
  }
  const activeColor = getColor(
    workspaces.findIndex((w) => w.id === activeWorkspace?.id),
  );
  return (
    <div ref={ref} className="relative px-3 py-2">
       
      <button
        onClick={() => setOpen(!open)}
        className={`flex w-full items-center gap-3 rounded-2xl border border-white/10 bg-white/5 p-3 text-left text-white transition-all hover:bg-white/10 active:scale-[0.98] ${collapsed ? "justify-center" : ""}`}
      >
         
        <div
          className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br ${activeColor || "from-cyan-400 to-indigo-500"}`}
        >
           
          <Building2 className="h-5 w-5 text-white" /> 
        </div> 
        {!collapsed && (
          <div className="min-w-0 flex-1">
             
            <p className="truncate text-sm font-semibold">
               
              {activeWorkspace?.name || "Select Workspace"} 
            </p> 
            <p className="text-xs text-slate-400">
              {activeWorkspace?.role?.replace("WORKSPACE_", "") || ""}
            </p> 
          </div>
        )} 
        {!collapsed && (
          <ChevronDown
            className={`h-4 w-4 shrink-0 text-slate-400 transition-transform ${open ? "rotate-180" : ""}`}
          />
        )} 
      </button> 
      {open && (
        <div className="absolute left-3 right-3 top-full z-50 mt-1 overflow-hidden rounded-2xl border border-slate-700 bg-slate-800 shadow-2xl">
           
          <div className="flex items-center gap-2 border-b border-slate-700 px-3 py-2">
             
            <Search className="h-4 w-4 text-slate-400 shrink-0" /> 
            <input
              ref={searchInputRef}
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Filter workspaces..."
              className="w-full bg-transparent text-sm text-white outline-none placeholder-slate-400"
            /> 
          </div> 
          <div className="max-h-60 overflow-y-auto py-1">
             
            {recentWorkspaces.length > 0 && !searchQuery && (
              <>
                 
                <p className="px-4 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-slate-500">
                   
                  <Clock className="h-3 w-3 inline mr-1" /> Recent 
                </p> 
                {recentWorkspaces.slice(0, 3).map((ws) => {
                  const color = getColor(
                    workspaces.findIndex((w) => w.id === ws.id),
                  );
                  return (
                    <button
                      key={`recent-${ws.id}`}
                      onClick={() => handleSelect(ws)}
                      className="flex w-full items-center gap-3 px-4 py-2.5 text-left text-sm text-slate-200 transition hover:bg-white/10"
                    >
                       
                      <div
                        className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br ${color || "from-cyan-400 to-indigo-500"} text-xs font-bold text-white`}
                      >
                         
                        {ws.name?.charAt(0).toUpperCase() || "W"} 
                      </div> 
                      <div className="min-w-0 flex-1">
                         
                        <p className="font-medium truncate text-xs">
                          {ws.name}
                        </p> 
                      </div> 
                    </button>
                  );
                })} 
                <div className="border-t border-slate-700/50 my-1" /> 
              </>
            )} 
            <p className="px-4 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-slate-500">
               
              All Workspaces 
            </p> 
            {filteredWorkspaces.length === 0 ? (
              <div className="px-4 py-4 text-center text-sm text-slate-400">
                No workspaces found
              </div>
            ) : (
              filteredWorkspaces.map((ws) => {
                const color = getColor(
                  workspaces.findIndex((w) => w.id === ws.id),
                );
                return (
                  <button
                    key={ws.id}
                    onClick={() => handleSelect(ws)}
                    className={`flex w-full items-center gap-3 px-4 py-2.5 text-left text-sm transition hover:bg-white/10 ${activeWorkspace?.id === ws.id ? "bg-cyan-500/10 text-cyan-300" : "text-slate-200"}`}
                  >
                     
                    <div
                      className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br ${color || "from-cyan-400 to-indigo-500"} text-xs font-bold text-white`}
                    >
                       
                      {ws.name?.charAt(0).toUpperCase() || "W"} 
                    </div> 
                    <div className="min-w-0 flex-1">
                       
                      <p className="font-medium truncate">{ws.name}</p> 
                      <p className="text-xs text-slate-400">
                        {ws.role?.replace("WORKSPACE_", "")}
                      </p> 
                    </div> 
                    {activeWorkspace?.id === ws.id && (
                      <Check className="h-4 w-4 shrink-0 text-cyan-400" />
                    )} 
                  </button>
                );
              })
            )} 
          </div> 
          <div className="border-t border-slate-700 px-2 py-2">
             
            <button
              onClick={() => {
                setOpen(false);
                navigate("/workspaces");
              }}
              className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-300 transition hover:bg-white/10 hover:text-white"
            >
               
              <Plus className="h-4 w-4" /> <span>Manage Workspaces</span> 
            </button> 
          </div> 
        </div>
      )} 
    </div>
  );
}
export function getActiveWorkspaceId() {
  return Number(localStorage.getItem("activeWorkspaceId")) || null;
}
