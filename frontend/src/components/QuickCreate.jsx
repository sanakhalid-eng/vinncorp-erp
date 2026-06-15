import { useState, useRef, useEffect } from "react";
import {
  Plus,
  CheckCircle2,
  FolderKanban,
  Trophy,
  Clock,
  X,
} from "lucide-react";
import { useNavigate, useLocation } from "react-router-dom";
import { useWorkspace } from "../context/WorkspaceContext";
import { useShortcut } from "../hooks/useKeyboardShortcuts";

const ACTIONS = [
  {
    id: "task",
    label: "New Task",
    icon: CheckCircle2,
    color: "bg-success-500 hover:bg-success-600",
    shortcut: "N",
    route: "create-task",
  },
  {
    id: "project",
    label: "New Project",
    icon: FolderKanban,
    color: "bg-primary-500 hover:bg-primary-600",
    shortcut: "Meta+N",
    route: "create-project",
  },
  {
    id: "sprint",
    label: "New Sprint",
    icon: Trophy,
    color: "bg-purple-500 hover:bg-purple-600",
    route: "sprints",
  },
  {
    id: "time",
    label: "Log Time",
    icon: Clock,
    color: "bg-warning-500 hover:bg-warning-600",
    route: "timesheet",
  },
];

export default function QuickCreate() {
  const [open, setOpen] = useState(false);
  const menuRef = useRef(null);
  const navigate = useNavigate();
  const location = useLocation();
  const { workspace } = useWorkspace();

  const isInputFocused = () => {
    const active = document.activeElement;
    return (
      active &&
      (active.tagName === "INPUT" ||
        active.tagName === "TEXTAREA" ||
        active.contentEditable === "true")
    );
  };

  const navigateTo = (route) => {
    setOpen(false);
    const slug = workspace?.slug;
    if (slug) {
      navigate(`/w/${slug}/${route}`);
    }
  };

  useShortcut("N", () => {
    if (!open && !isInputFocused()) {
      navigateTo(ACTIONS[0].route);
    }
  }, true);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const isWorkspacePage = location.pathname.startsWith("/w/");

  if (!isWorkspacePage) return null;

  return (
    <div
      ref={menuRef}
      className="fixed bottom-6 right-6 z-40 md:bottom-8 md:right-8"
    >
      {open && (
        <div className="absolute bottom-16 right-0 mb-2 bg-white dark:bg-surface-900 rounded-2xl shadow-soft-lg border border-surface-200 dark:border-surface-700 p-2 w-56 animate-slide-up">
          <div className="flex items-center justify-between px-3 py-2 border-b border-surface-100 dark:border-surface-800 mb-1">
            <span className="text-xs font-semibold text-surface-500 dark:text-surface-400 uppercase tracking-wider">
              Quick Create
            </span>
            <button
              onClick={() => setOpen(false)}
              className="p-1 rounded-lg hover:bg-surface-100 dark:hover:bg-surface-800 text-surface-400"
            >
              <X className="h-3.5 w-3.5" />
            </button>
          </div>
          <div className="space-y-0.5">
            {ACTIONS.map((action) => {
              const Icon = action.icon;
              return (
                <button
                  key={action.id}
                  onClick={() => navigateTo(action.route)}
                  className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-800 transition-colors"
                >
                  <div
                    className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${action.color} text-white`}
                  >
                    <Icon className="h-4 w-4" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium">{action.label}</p>
                  </div>
                  {action.shortcut && (
                    <kbd className="text-[10px] text-surface-400 font-mono">
                      {action.shortcut.replace("Meta+", "\u2318")}
                    </kbd>
                  )}
                </button>
              );
            })}
          </div>
        </div>
      )}

      <button
        onClick={() => setOpen(!open)}
        className={`flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-primary-500 to-primary-600 text-white shadow-lg hover:shadow-xl transition-all hover:scale-105 active:scale-95 ${
          open ? "rotate-45" : ""
        }`}
        aria-label="Quick create"
      >
        <Plus className="h-6 w-6" />
      </button>
    </div>
  );
}
