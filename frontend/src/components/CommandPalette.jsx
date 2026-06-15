import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import {
  Search,
  FolderKanban,
  CheckCircle2,
  Users,
  MessageSquare,
  Loader2,
  Clock,
  Zap,
  Plus,
  BarChart3,
  Trophy,
  Calendar,
  Settings2,
  ArrowRight,
  Command,
  X,
  Briefcase,
  Building2,
  Globe,
} from "lucide-react";
import { globalSearch } from "../api/searchApi";
import {
  getCommandPaletteRecent,
  recordCommandPaletteRecent,
  searchCommandPalette,
} from "../api/notesApi";
import { useWorkspace } from "../context/WorkspaceContext";
import {
  useShortcut,
  getShortcutLabel,
  SHORTCUTS,
} from "../hooks/useKeyboardShortcuts";
const TYPE_ICONS = {
  project: FolderKanban,
  task: CheckCircle2,
  member: Users,
  comment: MessageSquare,
  employee: Briefcase,
  department: Building2,
  workspace: Globe,
};
const TYPE_COLORS = {
  project:
    "text-primary-600 bg-primary-100 dark:bg-primary-900/30 dark:text-primary-400",
  task: "text-success-600 bg-success-100 dark:bg-success-900/30 dark:text-success-400",
  member:
    "text-purple-600 bg-purple-100 dark:bg-purple-900/30 dark:text-purple-400",
  comment:
    "text-warning-600 bg-warning-100 dark:bg-warning-900/30 dark:text-warning-400",
  employee:
    "text-blue-600 bg-blue-100 dark:bg-blue-900/30 dark:text-blue-400",
  department:
    "text-amber-600 bg-amber-100 dark:bg-amber-900/30 dark:text-amber-400",
  workspace:
    "text-cyan-600 bg-cyan-100 dark:bg-cyan-900/30 dark:text-cyan-400",
};
const QUICK_ACTIONS = [
  {
    label: "Create Task",
    icon: Plus,
    shortcut: "N",
    action: "create-task",
    category: "Tasks",
  },
  {
    label: "Create Project",
    icon: FolderKanban,
    shortcut: "Meta+N",
    action: "create-project",
    category: "Projects",
  },
  {
    label: "Go to Dashboard",
    icon: BarChart3,
    action: "go-dashboard",
    category: "Navigation",
  },
  {
    label: "Go to Tasks",
    icon: CheckCircle2,
    action: "go-tasks",
    category: "Navigation",
  },
  {
    label: "Go to Projects",
    icon: FolderKanban,
    action: "go-projects",
    category: "Navigation",
  },
  {
    label: "Go to Sprints",
    icon: Trophy,
    action: "go-sprints",
    category: "Navigation",
  },
  {
    label: "Go to Analytics",
    icon: BarChart3,
    action: "go-analytics",
    category: "Navigation",
  },
  {
    label: "Go to Calendar",
    icon: Calendar,
    action: "go-calendar",
    category: "Navigation",
  },
  {
    label: "Go to Settings",
    icon: Settings2,
    action: "go-settings",
    category: "Navigation",
  },
];
export default function CommandPalette({ onCreateTask, onCreateProject }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState(null);
  const [commands, setCommands] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedIdx, setSelectedIdx] = useState(0);
  const [sectionOffsets, setSectionOffsets] = useState({});
  const inputRef = useRef(null);
  const listRef = useRef(null);
  const navigate = useNavigate();
  const { workspace } = useWorkspace();
  const allHits = useMemo(() => {
    if (!results) return [];
    return [
      ...(results.tasks || []),
      ...(results.projects || []),
      ...(results.members || []),
      ...(results.comments || []),
      ...(results.employees || []),
      ...(results.departments || []),
      ...(results.workspaces || []),
    ];
  }, [results]);
  const filteredActions = useMemo(() => {
    if (!query.trim()) return QUICK_ACTIONS;
    const q = query.toLowerCase();
    return QUICK_ACTIONS.filter(
      (a) =>
        a.label.toLowerCase().includes(q) ||
        a.category.toLowerCase().includes(q),
    );
  }, [query]);
  const totalItems = useMemo(() => {
    return (
      allHits.length +
      (query.trim() ? 0 : filteredActions.length) +
      commands.length
    );
  }, [allHits, filteredActions, commands]);
  useShortcut(SHORTCUTS.COMMAND_PALETTE, () => setOpen((prev) => !prev), true);
  useShortcut(
    SHORTCUTS.COMMAND_PALETTE_WIN,
    () => setOpen((prev) => !prev),
    true,
  );
  useShortcut("Escape", () => setOpen(false), open);
  useEffect(() => {
    if (open && inputRef.current) {
      inputRef.current.focus();
    }
  }, [open]);
  useEffect(() => {
    if (!open) return;
    const timer = setTimeout(async () => {
      setLoading(true);
      try {
        if (!query.trim()) {
          const res = await getCommandPaletteRecent();
          setCommands(res.data.data || []);
          setResults(null);
        } else {
          const [searchRes, cmdRes] = await Promise.all([
            globalSearch(query),
            searchCommandPalette(query),
          ]);
          setResults(searchRes.data.data);
          setCommands(cmdRes.data.data || []);
        }
      } catch {
        setResults(null);
        setCommands([]);
      } finally {
        setLoading(false);
      }
    }, 200);
    return () => clearTimeout(timer);
  }, [query, open]);
  const handleSelect = useCallback(
    (hit) => {
      setOpen(false);
      setQuery("");
      setSelectedIdx(0);
      if (hit.action) {
        switch (hit.action) {
          case "create-task":
            onCreateTask?.();
            break;
          case "create-project":
            onCreateProject?.();
            break;
          case "go-dashboard":
            navigate(`/w/${workspace?.slug}/dashboard`);
            break;
          case "go-tasks":
            navigate(`/w/${workspace?.slug}/tasks`);
            break;
          case "go-projects":
            navigate(`/w/${workspace?.slug}/projects`);
            break;
          case "go-sprints":
            navigate(`/w/${workspace?.slug}/sprints`);
            break;
          case "go-analytics":
            navigate(`/w/${workspace?.slug}/analytics`);
            break;
          case "go-calendar":
            navigate(`/w/${workspace?.slug}/calendar`);
            break;
          case "go-settings":
            navigate(`/w/${workspace?.slug}/settings`);
            break;
        }
        return;
      }
      if (hit.url || hit.targetUrl) {
        const url = hit.url || hit.targetUrl;
        if (hit.actionKey) {
          recordCommandPaletteRecent({
            actionKey: hit.actionKey,
            actionLabel: hit.actionLabel || hit.title,
            targetUrl: url,
          }).catch(() => {});
        }
        navigate(url);
      }
    },
    [navigate, workspace, onCreateTask, onCreateProject],
  );
  const handleKeyDown = useCallback(
    (e) => {
      if (
        e.key === "ArrowDown" ||
        (e.key === "j" &&
          !e.ctrlKey &&
          !e.metaKey &&
          document.activeElement === inputRef.current)
      ) {
        e.preventDefault();
        setSelectedIdx((prev) => Math.min(prev + 1, totalItems - 1));
      } else if (
        e.key === "ArrowUp" ||
        (e.key === "k" &&
          !e.ctrlKey &&
          !e.metaKey &&
          document.activeElement === inputRef.current)
      ) {
        e.preventDefault();
        setSelectedIdx((prev) => Math.max(prev - 1, 0));
      } else if (e.key === "Enter") {
        e.preventDefault();
        const items = getVisibleItems();
        if (items[selectedIdx]) handleSelect(items[selectedIdx]);
      }
    },
    [selectedIdx, totalItems, handleSelect],
  );
  const getVisibleItems = useCallback(() => {
    if (query.trim()) {
      return allHits;
    }
    return [...filteredActions, ...commands];
  }, [query, allHits, filteredActions, commands]);
  if (!open) return null;
  const hasResults =
    results &&
    (results.tasks?.length ||
      results.projects?.length ||
      results.members?.length ||
      results.comments?.length ||
      results.employees?.length ||
      results.departments?.length ||
      results.workspaces?.length);
  return (
    <div className="fixed inset-0 z-[100] flex items-start justify-center pt-[10vh] md:pt-[15vh] px-4">
       
      <div
        className="fixed inset-0 bg-black/50 backdrop-blur-sm animate-fade-in"
        onClick={() => setOpen(false)}
      /> 
      <div className="relative w-full max-w-2xl rounded-2xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-900 shadow-soft-lg overflow-hidden animate-scale-in">
         
        <div className="flex items-center gap-3 border-b border-surface-200 dark:border-surface-700 px-4 py-3">
           
          <Search className="h-5 w-5 text-surface-400 shrink-0" /> 
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setSelectedIdx(0);
            }}
            onKeyDown={handleKeyDown}
            placeholder={`Search ${workspace?.name || "workspace"}...`}
            className="flex-1 outline-none text-base text-surface-800 dark:text-surface-200 placeholder-surface-400 bg-transparent"
          /> 
          {loading && (
            <Loader2 className="h-4 w-4 animate-spin text-surface-400" />
          )} 
          <div className="hidden sm:flex items-center gap-1">
             
            <kbd className="inline-flex items-center rounded-md border border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-800 px-2 py-0.5 text-xs text-surface-400">
               
              {getShortcutLabel("Esc")} 
            </kbd> 
          </div> 
        </div> 
        <div
          ref={listRef}
          className="max-h-96 overflow-y-auto p-2 scrollbar-thin"
        >
           
          {query.trim() && !loading && (
            <>
               
              {!hasResults && commands.length === 0 ? (
                <div className="py-12 text-center text-sm text-surface-400">
                   
                  <Search className="h-8 w-8 mx-auto mb-3 opacity-50" /> No
                  results found for "{query}" 
                </div>
              ) : (
                <div className="space-y-1">
                   
                  {results.tasks?.map((hit, i) => (
                    <SearchRow
                      key={`t-${hit.id}`}
                      hit={hit}
                      index={i}
                      selectedIdx={selectedIdx}
                      onSelect={handleSelect}
                    />
                  ))} 
                  {results.projects?.map((hit, i) => (
                    <SearchRow
                      key={`p-${hit.id}`}
                      hit={hit}
                      index={i + (results.tasks?.length || 0)}
                      selectedIdx={selectedIdx}
                      onSelect={handleSelect}
                    />
                  ))} 
                  {results.members?.map((hit, i) => (
                    <SearchRow
                      key={`m-${hit.id}`}
                      hit={hit}
                      index={
                        i +
                        (results.tasks?.length || 0) +
                        (results.projects?.length || 0)
                      }
                      selectedIdx={selectedIdx}
                      onSelect={handleSelect}
                    />
                  ))} 
                  {results.comments?.map((hit, i) => (
                    <SearchRow
                      key={`c-${hit.id}`}
                      hit={hit}
                      index={
                        i +
                        (results.tasks?.length || 0) +
                        (results.projects?.length || 0) +
                        (results.members?.length || 0)
                      }
                      selectedIdx={selectedIdx}
                      onSelect={handleSelect}
                    />
                  ))}
                  {results.employees?.map((hit, i) => (
                    <SearchRow
                      key={`e-${hit.id}`}
                      hit={hit}
                      index={
                        i +
                        (results.tasks?.length || 0) +
                        (results.projects?.length || 0) +
                        (results.members?.length || 0) +
                        (results.comments?.length || 0)
                      }
                      selectedIdx={selectedIdx}
                      onSelect={handleSelect}
                    />
                  ))}
                  {results.departments?.map((hit, i) => (
                    <SearchRow
                      key={`d-${hit.id}`}
                      hit={hit}
                      index={
                        i +
                        (results.tasks?.length || 0) +
                        (results.projects?.length || 0) +
                        (results.members?.length || 0) +
                        (results.comments?.length || 0) +
                        (results.employees?.length || 0)
                      }
                      selectedIdx={selectedIdx}
                      onSelect={handleSelect}
                    />
                  ))}
                  {results.workspaces?.map((hit, i) => (
                    <SearchRow
                      key={`w-${hit.id}`}
                      hit={hit}
                      index={
                        i +
                        (results.tasks?.length || 0) +
                        (results.projects?.length || 0) +
                        (results.members?.length || 0) +
                        (results.comments?.length || 0) +
                        (results.employees?.length || 0) +
                        (results.departments?.length || 0)
                      }
                      selectedIdx={selectedIdx}
                      onSelect={handleSelect}
                    />
                  ))} 
                </div>
              )} 
            </>
          )} 
          {!query.trim() && (
            <>
               
              {filteredActions.length > 0 && (
                <section className="mb-2">
                   
                  <p className="px-3 py-2 text-xs font-semibold text-surface-400 uppercase tracking-wider flex items-center gap-2">
                     
                    <Zap className="h-3 w-3" /> Quick Actions 
                  </p> 
                  <div className="space-y-0.5">
                     
                    {filteredActions.map((action, i) => (
                      <ActionRow
                        key={action.action}
                        action={action}
                        index={i}
                        selectedIdx={selectedIdx}
                        onSelect={handleSelect}
                      />
                    ))} 
                  </div> 
                </section>
              )} 
              {commands.length > 0 && (
                <section>
                   
                  <p className="px-3 py-2 text-xs font-semibold text-surface-400 uppercase tracking-wider flex items-center gap-2">
                     
                    <Clock className="h-3 w-3" /> Recent 
                  </p> 
                  <div className="space-y-0.5">
                     
                    {commands.map((cmd, i) => (
                      <button
                        key={cmd.actionKey || i}
                        type="button"
                        onClick={() => handleSelect(cmd)}
                        className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left transition ${i + filteredActions.length === selectedIdx ? "bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-300" : "text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-800"}`}
                      >
                         
                        <span className="text-sm font-medium">
                          {cmd.actionLabel || cmd.label}
                        </span> 
                        {cmd.category && (
                          <span className="text-xs text-surface-400 ml-auto">
                            {cmd.category}
                          </span>
                        )} 
                      </button>
                    ))} 
                  </div> 
                </section>
              )} 
              {filteredActions.length === 0 &&
                commands.length === 0 &&
                !loading && (
                  <div className="py-12 text-center text-sm text-surface-400">
                     
                    <Command className="h-8 w-8 mx-auto mb-3 opacity-50" /> Type
                    to search tasks, projects, members, employees, departments, and more 
                  </div>
                )} 
            </>
          )} 
          {loading && (
            <div className="py-12 text-center">
               
              <Loader2 className="h-6 w-6 animate-spin mx-auto text-surface-400 mb-3" /> 
              <p className="text-sm text-surface-400">Searching...</p> 
            </div>
          )} 
        </div> 
        <div className="border-t border-surface-200 dark:border-surface-700 px-4 py-2 flex items-center gap-4 text-xs text-surface-400">
           
          <span className="flex items-center gap-1">
             
            <kbd className="px-1.5 py-0.5 rounded border border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-800">
              ↑↓
            </kbd> 
            Navigate 
          </span> 
          <span className="flex items-center gap-1">
             
            <kbd className="px-1.5 py-0.5 rounded border border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-800">
              ↵
            </kbd> 
            Select 
          </span> 
          <span className="flex items-center gap-1">
             
            <kbd className="px-1.5 py-0.5 rounded border border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-800">
              j/k
            </kbd> 
            Vim keys 
          </span> 
        </div> 
      </div> 
    </div>
  );
}
function SearchRow({ hit, index, selectedIdx, onSelect }) {
  const Icon = TYPE_ICONS[hit.type] || Search;
  const isSelected = index === selectedIdx;
  return (
    <button
      onClick={() => onSelect(hit)}
      className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left transition ${isSelected ? "bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-300" : "text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-800"}`}
    >
       
      <div
        className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${TYPE_COLORS[hit.type] || "text-surface-500 bg-surface-100 dark:bg-surface-800"}`}
      >
         
        <Icon className="h-4 w-4" /> 
      </div> 
      <div className="min-w-0 flex-1">
         
        <p className="text-sm font-medium truncate">{hit.title}</p> 
        {hit.subtitle && (
          <p className="text-xs text-surface-400 truncate">{hit.subtitle}</p>
        )} 
      </div> 
      {hit.badge && (
        <span className="shrink-0 rounded-full bg-surface-100 dark:bg-surface-800 px-2 py-0.5 text-xs text-surface-500">
           
          {hit.badge} 
        </span>
      )} 
      <ArrowRight className="h-4 w-4 text-surface-300 dark:text-surface-600 shrink-0 opacity-0 group-hover:opacity-100 transition-opacity" /> 
    </button>
  );
}
function ActionRow({ action, index, selectedIdx, onSelect }) {
  const Icon = action.icon;
  const isSelected = index === selectedIdx;
  return (
    <button
      onClick={() => onSelect(action)}
      className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left transition ${isSelected ? "bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-300" : "text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-800"}`}
    >
       
      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-400">
         
        <Icon className="h-4 w-4" /> 
      </div> 
      <div className="min-w-0 flex-1">
         
        <p className="text-sm font-medium">{action.label}</p> 
        <p className="text-xs text-surface-400">{action.category}</p> 
      </div> 
      {action.shortcut && (
        <kbd className="shrink-0 rounded-md border border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-800 px-2 py-0.5 text-xs text-surface-400">
           
          {getShortcutLabel(action.shortcut)} 
        </kbd>
      )} 
    </button>
  );
}
