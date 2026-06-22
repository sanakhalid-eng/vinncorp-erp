import { useMemo, useState, useEffect, useRef, useCallback } from "react";
import { NavLink, useNavigate, useLocation } from "react-router-dom";
import {
  Activity,
  Briefcase,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  FolderKanban,
  LayoutGrid,
  LogOut,
  Settings2,
  Shield,
  UserCircle2,
  Users,
  X,
  Trophy,
  BarChart3,
  Calendar,
  Webhook,
  MessageSquare,
  Server,
  ClipboardList,
  HeartPulse,
  Command,
  Building2,
  Zap,
  BookOpen,
  StickyNote,
  LineChart,
  Menu,
  GripVertical,
  GanttChartSquare,
  User,
  TreePalm,
  FileText,
  TrendingUp,
  Phone,
  UsersRound,
  Contact,
  GitBranch,
  Target,
  DollarSign,
  Receipt,
  CreditCard,
  Wallet,
  ClipboardCheck,
} from "lucide-react";
import { useAuth } from "../context/useAuth.js";
import { usePermission } from "../context/usePermission.js";
import {
  DASHBOARD_VIEW,
  MEMBER_VIEW,
  USER_VIEW,
  ROLE_VIEW,
  CRM_VIEW,
  LEAD_VIEW,
  CUSTOMER_VIEW,
  CONTACT_VIEW,
  DEAL_VIEW,
  PIPELINE_VIEW,
  EMPLOYEE_VIEW,
  DEPARTMENT_VIEW,
  DESIGNATION_VIEW,
  FINANCE_VIEW,
  PROJECT_VIEW,
  PROJECT_VIEW_ALL,
  TASK_VIEW,
  TASK_VIEW_ALL,
  WORKFLOW_MANAGE,
  TIMESHEET_APPROVE,
} from "../constants/permissions.js";
import { filterByAccess } from "../utils/accessControl.js";
import { useWorkspace } from "../context/WorkspaceContext";
import WorkspaceSwitcher from "../features/settings/components/WorkspaceSwitcher";
import { useIsDesktop } from "../hooks/useBreakpoint";
const getInitials = (name) => (name ? name.charAt(0).toUpperCase() : "U");
const PROJECT_PATH_REGEX = /\/projects\/(\d+)/;
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
const SIDEBAR_MIN_WIDTH = 72;
const SIDEBAR_EXPANDED_WIDTH = 288;
const STORAGE_KEY_COLLAPSED = "sidebar_collapsed";
const STORAGE_KEY_WIDTH = "sidebar_width";
function SidebarContent({
  collapsed,
  onToggle,
  onNavigate,
  onClose,
  width,
  onResizeStart,
}) {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, globalRoles } = useAuth();
  const permission = usePermission();
  const accessContext = {
    hasPermission: permission.hasPermission,
    hasAnyPermission: permission.hasAnyPermission,
    hasRole: permission.hasRole,
    hasAnyRole: permission.hasAnyRole,
    hasMinRoleLevel: permission.hasMinRoleLevel,
    isAdmin: permission.isAdmin,
    isSuperAdmin: permission.isSuperAdmin,
    globalRoles,
    user,
  };
  const { workspace, workspaces } = useWorkspace();
  const [hovered, setHovered] = useState(false);
  const currentProjectId = useMemo(() => {
    const match = location.pathname.match(PROJECT_PATH_REGEX);
    return match ? match[1] : null;
  }, [location.pathname]);
  const workspaceSlug = workspace?.slug || "";
  const profileRole = useMemo(() => {
    const primaryRole = user?.roles?.[0];
    return primaryRole
      ? primaryRole
          .toLowerCase()
          .split("_")
          .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
          .join(" ")
      : "Workspace Member";
  }, [user?.roles]);
  const wsNavItems = filterByAccess(
    [
      {
        name: "Dashboard",
        path: `/w/${workspaceSlug}/dashboard`,
        icon: LayoutGrid,
        hint: "Overview",
        requiresPermission: DASHBOARD_VIEW,
      },
      {
        name: "Projects",
        path: `/w/${workspaceSlug}/projects`,
        icon: FolderKanban,
        hint: "Delivery",
        requiresAnyPermission: [PROJECT_VIEW, PROJECT_VIEW_ALL],
      },
      {
        name: "Tasks",
        path: `/w/${workspaceSlug}/tasks`,
        icon: CheckCircle2,
        hint: "Execution",
        requiresAnyPermission: [TASK_VIEW, TASK_VIEW_ALL],
      },
      {
        name: "Gantt",
        path: `/w/${workspaceSlug}/gantt`,
        icon: GanttChartSquare,
        hint: "Timeline",
        requiresAnyPermission: [TASK_VIEW, TASK_VIEW_ALL],
      },
      {
        name: "Activity",
        path: `/w/${workspaceSlug}/activity`,
        icon: Activity,
        hint: "Feed",
      },
      {
        name: "Automation",
        path: `/w/${workspaceSlug}/automation-rules`,
        icon: Zap,
        hint: "Workflow Rules",
        requiresPermission: WORKFLOW_MANAGE,
      },
      {
        name: "Analytics",
        path: `/w/${workspaceSlug}/analytics`,
        icon: BarChart3,
        hint: "Insights",
        requiresPermission: DASHBOARD_VIEW,
      },
      {
        name: "Executive",
        path: `/w/${workspaceSlug}/insights`,
        icon: LineChart,
        hint: "Leadership",
        requiresPermission: DASHBOARD_VIEW,
      },
      {
        name: "Knowledge",
        path: `/w/${workspaceSlug}/knowledge`,
        icon: BookOpen,
        hint: "Docs",
      },
      {
        name: "Notes",
        path: `/w/${workspaceSlug}/notes`,
        icon: StickyNote,
        hint: "Wiki",
      },
      {
        name: "Members",
        path: `/w/${workspaceSlug}/members`,
        icon: Users,
        hint: "People",
        requiresPermission: MEMBER_VIEW,
      },
      {
        name: "Employees",
        path: `/w/${workspaceSlug}/hr/employees`,
        icon: Briefcase,
        hint: "HR",
        requiresPermission: EMPLOYEE_VIEW,
      },
      {
        name: "Departments",
        path: `/w/${workspaceSlug}/hr/departments`,
        icon: Building2,
        hint: "Org Structure",
        requiresPermission: DEPARTMENT_VIEW,
      },
      {
        name: "Designations",
        path: `/w/${workspaceSlug}/hr/designations`,
        icon: Shield,
        hint: "Job Levels",
        requiresPermission: DESIGNATION_VIEW,
      },
      {
        name: "CRM Dashboard",
        path: `/w/${workspaceSlug}/crm`,
        icon: Target,
        hint: "CRM",
        requiresPermission: CRM_VIEW,
      },
      {
        name: "Leads",
        path: `/w/${workspaceSlug}/crm/leads`,
        icon: Phone,
        hint: "Prospects",
        requiresPermission: LEAD_VIEW,
      },
      {
        name: "Customers",
        path: `/w/${workspaceSlug}/crm/customers`,
        icon: UsersRound,
        hint: "Clients",
        requiresPermission: CUSTOMER_VIEW,
      },
      {
        name: "Contacts",
        path: `/w/${workspaceSlug}/crm/contacts`,
        icon: Contact,
        hint: "People",
        requiresPermission: CONTACT_VIEW,
      },
      {
        name: "Opportunities",
        path: `/w/${workspaceSlug}/crm/opportunities`,
        icon: Briefcase,
        hint: "Deals",
        requiresPermission: DEAL_VIEW,
      },
      {
        name: "Pipeline",
        path: `/w/${workspaceSlug}/crm/pipeline`,
        icon: GitBranch,
        hint: "Sales",
        requiresPermission: PIPELINE_VIEW,
      },
      {
        name: "Finance",
        path: `/w/${workspaceSlug}/finance`,
        icon: DollarSign,
        hint: "Overview",
        requiresPermission: FINANCE_VIEW,
      },
      {
        name: "Invoices",
        path: `/w/${workspaceSlug}/finance/invoices`,
        icon: Receipt,
        hint: "Billing",
        requiresPermission: FINANCE_VIEW,
      },
      {
        name: "Payments",
        path: `/w/${workspaceSlug}/finance/payments`,
        icon: CreditCard,
        hint: "Collections",
        requiresPermission: FINANCE_VIEW,
      },
      {
        name: "Expenses",
        path: `/w/${workspaceSlug}/finance/expenses`,
        icon: Wallet,
        hint: "Spend",
        requiresPermission: FINANCE_VIEW,
      },
      {
        name: "Settings",
        path: `/w/${workspaceSlug}/settings`,
        icon: Settings2,
        hint: "Configuration",
        requiresAdmin: true,
      },
    ],
    accessContext,
  );

  const adminNavItems = filterByAccess(
    [
      {
        name: "Users",
        path: `/w/${workspaceSlug}/users`,
        icon: Users,
        hint: "Accounts",
        requiresPermission: USER_VIEW,
      },
      {
        name: "Roles",
        path: `/w/${workspaceSlug}/roles`,
        icon: Shield,
        hint: "Access",
        requiresPermission: ROLE_VIEW,
      },
      {
        name: "Audit Logs",
        path: `/w/${workspaceSlug}/audit-logs`,
        icon: ClipboardList,
        hint: "History",
        requiresPermission: ROLE_VIEW,
      },
      {
        name: "Timesheet Approvals",
        path: `/w/${workspaceSlug}/timesheet-approvals`,
        icon: ClipboardCheck,
        hint: "Review",
        requiresPermission: TIMESHEET_APPROVE,
      },
    ],
    accessContext,
  );
  const effectiveCollapsed = collapsed && !hovered;
  const sidebarWidth = effectiveCollapsed
    ? SIDEBAR_MIN_WIDTH
    : width || SIDEBAR_EXPANDED_WIDTH;
  return (
    <aside
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      className={`relative flex h-full flex-col border-r border-slate-200/10 bg-[linear-gradient(180deg,_rgba(15,23,42,1)_0%,_rgba(30,41,59,1)_56%,_rgba(51,65,85,1)_100%)] text-white shadow-xl transition-[width] duration-300`}
      style={{ width: sidebarWidth }}
    >
       
      {!effectiveCollapsed && (
        <div
          onMouseDown={onResizeStart}
          className="absolute right-0 top-0 z-50 h-full w-1.5 cursor-col-resize hover:w-2 hover:bg-cyan-400/40 active:bg-cyan-400/60 transition-all group"
        >
           
          <div className="absolute right-0 top-1/2 -translate-y-1/2 hidden group-hover:flex flex-col gap-0.5 px-0.5">
             
            <div className="h-4 w-0.5 rounded-full bg-cyan-400/60" /> 
            <div className="h-4 w-0.5 rounded-full bg-cyan-400/60" /> 
            <div className="h-4 w-0.5 rounded-full bg-cyan-400/60" /> 
          </div> 
        </div>
      )} 
      <div className="flex items-center justify-between border-b border-white/10 px-4 py-4">
         
        <button
          onClick={() => {
            navigate(`/w/${workspaceSlug}/dashboard`);
            onClose?.();
          }}
          className="flex items-center gap-3 text-left"
        >
           
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-400 to-indigo-500 shadow-lg shadow-cyan-500/20">
             
            <Briefcase className="h-5 w-5 text-white" /> 
          </div> 
          {!effectiveCollapsed && (
            <div>
               
              <p className="text-[10px] uppercase tracking-[0.28em] text-slate-300">
                PMT-SK
              </p> 
              <h1 className="text-sm font-bold leading-tight">
                Project Command
              </h1> 
            </div>
          )} 
        </button> 
        <div className="flex items-center gap-1">
           
          {onClose && (
            <button
              onClick={onClose}
              className="rounded-lg border border-white/10 bg-white/5 p-1.5 text-slate-200 transition hover:bg-white/10 lg:hidden"
              aria-label="Close sidebar"
            >
               
              <X className="h-4 w-4" /> 
            </button>
          )} 
          <button
            onClick={onToggle}
            className="hidden lg:block rounded-lg border border-white/10 bg-white/5 p-1.5 text-slate-200 transition hover:bg-white/10"
            aria-label="Toggle sidebar"
          >
             
            {effectiveCollapsed ? (
              <ChevronRight className="h-4 w-4" />
            ) : (
              <ChevronLeft className="h-4 w-4" />
            )} 
          </button> 
        </div> 
      </div> 
      <div className="border-b border-white/10 px-3 py-3">
         
        <button
          onClick={() => {
            navigate("/profile");
            onNavigate?.();
            onClose?.();
          }}
          className={`flex w-full items-center gap-3 rounded-xl border border-white/10 bg-white/5 p-2 text-left transition hover:bg-white/10 ${effectiveCollapsed ? "justify-center" : ""}`}
        >
           
          <div className="flex h-9 w-9 shrink-0 items-center justify-center overflow-hidden rounded-xl bg-gradient-to-br from-indigo-500 to-fuchsia-500 text-sm font-bold text-white">
             
            {user?.avatarUrl ? (
              <img
                src={user.avatarUrl}
                alt="Profile"
                className="h-full w-full object-cover"
              />
            ) : (
              getInitials(user?.name)
            )} 
          </div> 
          {!effectiveCollapsed && (
            <div className="min-w-0 flex-1">
               
              <p className="truncate font-semibold text-white text-sm">
                {user?.name}
              </p> 
              <p className="truncate text-xs text-slate-300">
                {profileRole}
              </p> 
            </div>
          )} 
        </button> 
      </div> 
      <div className="border-b border-white/10">
         
        <WorkspaceSwitcher collapsed={effectiveCollapsed} /> 
      </div> 
      {workspace && !effectiveCollapsed && (
        <div className="border-b border-white/10 px-3 py-2">
           
          <div className="flex items-center gap-2 rounded-xl bg-white/5 px-2 py-2">
             
            <div
              className={`flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br ${getColor(workspaces.findIndex((w) => w.id === workspace.id))} text-xs font-bold text-white`}
            >
               
              {workspace.name?.charAt(0).toUpperCase() || "W"} 
            </div> 
            <div className="min-w-0 flex-1">
               
              <p className="text-xs font-medium text-white truncate">
                {workspace.name}
              </p> 
              <p className="text-[10px] text-slate-400">

                {workspace.memberCount || 0} members
              </p>
            </div> 
          </div> 
        </div>
      )} 

      {/* SUPER_ADMIN Platform Navigation */}
      {globalRoles?.includes("SUPER_ADMIN") && !workspace && (
        <div className="flex-1 overflow-y-auto px-2 py-4 scrollbar-thin">
          <div className="space-y-4">
            <div>
              {!effectiveCollapsed && (
                <p className="mb-2 px-2 text-[10px] font-semibold uppercase tracking-[0.26em] text-slate-400">
                  Platform Admin
                </p>
              )}
              <div className="space-y-1">
                {[
                  { name: "Dashboard", path: "/admin/dashboard", icon: LayoutGrid, hint: "Overview" },
                  { name: "Workspaces", path: "/admin/workspaces", icon: Briefcase, hint: "Manage" },
                  { name: "Users", path: "/admin/users", icon: Users, hint: "People" },
                  { name: "Audit Logs", path: "/admin/audit-logs", icon: ClipboardList, hint: "Logs" },
                  { name: "System Settings", path: "/admin/settings", icon: Settings2, hint: "Config" },
                ].map((link) => {
                  const Icon = link.icon;
                  return (
                    <NavLink
                      key={link.path}
                      to={link.path}
                      onClick={() => {
                        onNavigate?.();
                        onClose?.();
                      }}
                      className={({ isActive }) =>
                        `group flex items-center gap-2 rounded-xl px-2 py-2 transition ${effectiveCollapsed ? "justify-center" : ""} ${isActive ? "bg-gradient-to-r from-cyan-500/25 to-indigo-500/25 text-white shadow-lg ring-1 ring-cyan-400/30" : "text-slate-300 hover:bg-white/8 hover:text-white"}`
                      }
                    >
                      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/8 transition group-hover:bg-white/12">
                        <Icon className="h-4 w-4" />
                      </div>
                      {!effectiveCollapsed && (
                        <div className="min-w-0">
                          <p className="font-medium text-sm">{link.name}</p>
                          <p className="text-[10px] text-slate-400 group-hover:text-slate-300">
                            {link.hint}
                          </p>
                        </div>
                      )}
                    </NavLink>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Workspace Navigation (only shown when workspace is selected) */}
      {workspace && (
        <div className="flex-1 overflow-y-auto px-2 py-4 scrollbar-thin">
        <div className="space-y-4">
           
          <div>
             
            {!effectiveCollapsed && (
              <p className="mb-2 px-2 text-[10px] font-semibold uppercase tracking-[0.26em] text-slate-400">
                 
                Workspace 
              </p>
            )} 
            <div className="space-y-1">
               
              {wsNavItems.map((link) => {
                const Icon = link.icon;
                return (
                  <NavLink
                    key={link.path}
                    to={link.path}
                    onClick={() => {
                      onNavigate?.();
                      onClose?.();
                    }}
                    className={({ isActive }) =>
                      `group flex items-center gap-2 rounded-xl px-2 py-2 transition ${effectiveCollapsed ? "justify-center" : ""} ${isActive ? "bg-gradient-to-r from-cyan-500/25 to-indigo-500/25 text-white shadow-lg ring-1 ring-cyan-400/30" : "text-slate-300 hover:bg-white/8 hover:text-white"}`
                    }
                  >
                     
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/8 transition group-hover:bg-white/12">
                       
                      <Icon className="h-4 w-4" /> 
                    </div> 
                    {!effectiveCollapsed && (
                      <div className="min-w-0">
                         
                        <p className="font-medium text-sm">{link.name}</p> 
                        <p className="text-[10px] text-slate-400 group-hover:text-slate-300">
                          {link.hint}
                        </p> 
                      </div>
                    )} 
                  </NavLink>
                );
              })} 
            </div> 
          </div>

          <div>
            {!effectiveCollapsed && (
              <p className="mb-2 px-2 text-[10px] font-semibold uppercase tracking-[0.26em] text-slate-400">
                My Space
              </p>
            )}
            <div className="space-y-1">
              {[
                { name: "My Profile", path: `/w/${workspaceSlug}/my-profile`, icon: User, hint: "Profile" },
                { name: "My Attendance", path: `/w/${workspaceSlug}/my-attendance`, icon: Calendar, hint: "Attendance" },
                { name: "My Leaves", path: `/w/${workspaceSlug}/my-leaves`, icon: TreePalm, hint: "Leaves" },
                { name: "My Documents", path: `/w/${workspaceSlug}/my-documents`, icon: FileText, hint: "Documents" },
              ].map((link) => {
                const Icon = link.icon;
                return (
                  <NavLink
                    key={link.path}
                    to={link.path}
                    onClick={() => {
                      onNavigate?.();
                      onClose?.();
                    }}
                    className={({ isActive }) =>
                      `group flex items-center gap-2 rounded-xl px-2 py-2 transition ${effectiveCollapsed ? "justify-center" : ""} ${isActive ? "bg-gradient-to-r from-cyan-500/25 to-indigo-500/25 text-white shadow-lg ring-1 ring-cyan-400/30" : "text-slate-300 hover:bg-white/8 hover:text-white"}`
                    }
                  >
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/8 transition group-hover:bg-white/12">
                      <Icon className="h-4 w-4" />
                    </div>
                    {!effectiveCollapsed && (
                      <div className="min-w-0">
                        <p className="font-medium text-sm">{link.name}</p>
                        <p className="text-[10px] text-slate-400 group-hover:text-slate-300">
                          {link.hint}
                        </p>
                      </div>
                    )}
                  </NavLink>
                );
              })}
            </div>
          </div>

          {permission.hasPermission?.(EMPLOYEE_VIEW) && (
            <div>
              {!effectiveCollapsed && (
                <p className="mb-2 px-2 text-[10px] font-semibold uppercase tracking-[0.26em] text-slate-400">
                  HR Analytics
                </p>
              )}
              <div className="space-y-1">
                {[
                  { name: "HR Dashboard", path: `/w/${workspaceSlug}/hr/analytics`, icon: BarChart3, hint: "Insights" },
                  { name: "Utilization Report", path: `/w/${workspaceSlug}/hr/utilization`, icon: TrendingUp, hint: "Reports" },
                ].map((link) => {
                  const Icon = link.icon;
                  return (
                    <NavLink
                      key={link.path}
                      to={link.path}
                      onClick={() => {
                        onNavigate?.();
                        onClose?.();
                      }}
                      className={({ isActive }) =>
                        `group flex items-center gap-2 rounded-xl px-2 py-2 transition ${effectiveCollapsed ? "justify-center" : ""} ${isActive ? "bg-gradient-to-r from-cyan-500/25 to-indigo-500/25 text-white shadow-lg ring-1 ring-cyan-400/30" : "text-slate-300 hover:bg-white/8 hover:text-white"}`
                      }
                    >
                      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/8 transition group-hover:bg-white/12">
                        <Icon className="h-4 w-4" />
                      </div>
                      {!effectiveCollapsed && (
                        <div className="min-w-0">
                          <p className="font-medium text-sm">{link.name}</p>
                          <p className="text-[10px] text-slate-400 group-hover:text-slate-300">
                            {link.hint}
                          </p>
                        </div>
                      )}
                    </NavLink>
                  );
                })}
              </div>
            </div>
          )}

          {adminNavItems.length > 0 && (
            <div>
              {!effectiveCollapsed && (
                <p className="mb-2 px-2 text-[10px] font-semibold uppercase tracking-[0.26em] text-slate-400">
                  Administration
                </p>
              )}
              <div className="space-y-1">
                {adminNavItems.map((link) => {
                  const Icon = link.icon;
                  return (
                    <NavLink
                      key={link.path}
                      to={link.path}
                      onClick={() => {
                        onNavigate?.();
                        onClose?.();
                      }}
                      className={({ isActive }) =>
                        `group flex items-center gap-2 rounded-xl px-2 py-2 transition ${effectiveCollapsed ? "justify-center" : ""} ${isActive ? "bg-gradient-to-r from-cyan-500/25 to-indigo-500/25 text-white shadow-lg ring-1 ring-cyan-400/30" : "text-slate-300 hover:bg-white/8 hover:text-white"}`
                      }
                    >
                      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/8 transition group-hover:bg-white/12">
                        <Icon className="h-4 w-4" />
                      </div>
                      {!effectiveCollapsed && (
                        <div className="min-w-0">
                          <p className="font-medium text-sm">{link.name}</p>
                          <p className="text-[10px] text-slate-400 group-hover:text-slate-300">
                            {link.hint}
                          </p>
                        </div>
                      )}
                    </NavLink>
                  );
                })}
              </div>
            </div>
          )}
        </div> 
        </div>
      )}
      <div className="border-t border-white/10 px-3 py-3">
         
        <button
          onClick={() => {
            logout();
            navigate("/");
            onNavigate?.();
            onClose?.();
          }}
          className={`flex w-full items-center gap-2 rounded-xl border border-white/10 bg-white/5 px-2 py-2 text-slate-200 transition hover:bg-red-500/15 hover:text-white ${effectiveCollapsed ? "justify-center" : ""}`}
        >
           
          <LogOut className="h-4 w-4" /> 
          {!effectiveCollapsed && (
            <span className="font-medium text-sm">Logout</span>
          )} 
        </button> 
      </div> 
    </aside>
  );
}
export default function Sidebar() {
  const [collapsed, setCollapsed] = useState(
    () => localStorage.getItem(STORAGE_KEY_COLLAPSED) === "true",
  );
  const [sidebarWidth, setSidebarWidth] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY_WIDTH);
    return saved ? Number(saved) : SIDEBAR_EXPANDED_WIDTH;
  });
  const [mobileOpen, setMobileOpen] = useState(false);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [isResizing, setIsResizing] = useState(false);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const buttonRef = useRef(null);
  const isDesktop = useIsDesktop();
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY_COLLAPSED, String(collapsed));
  }, [collapsed]);
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY_WIDTH, String(sidebarWidth));
  }, [sidebarWidth]);
  useEffect(() => {
    if (isDesktop && mobileOpen) {
      setMobileOpen(false);
    }
  }, [isDesktop, mobileOpen]);
  useEffect(() => {
    if (mobileOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => {
      document.body.style.overflow = "";
    };
  }, [mobileOpen]);
  const handleDragStart = useCallback((e) => {
    const touch = e.touches?.[0] || e;
    const rect = buttonRef.current?.getBoundingClientRect();
    if (!rect) return;
    setIsDragging(true);
    setDragOffset({
      x: touch.clientX - rect.left,
      y: touch.clientY - rect.top,
    });
  }, []);
  const handleDragMove = useCallback(
    (e) => {
      if (!isDragging) return;
      e.preventDefault();
      const touch = e.touches?.[0] || e;
      const newX = Math.max(
        0,
        Math.min(window.innerWidth - 48, touch.clientX - dragOffset.x),
      );
      const newY = Math.max(
        0,
        Math.min(window.innerHeight - 48, touch.clientY - dragOffset.y),
      );
      setPosition({ x: newX, y: newY });
    },
    [isDragging, dragOffset],
  );
  const handleDragEnd = useCallback(() => {
    setIsDragging(false);
  }, []);
  const handleResizeStart = useCallback((e) => {
    e.preventDefault();
    setIsResizing(true);
  }, []);
  const handleResizeMove = useCallback(
    (e) => {
      if (!isResizing) return;
      const clientX = e.touches?.[0]?.clientX || e.clientX;
      const newWidth = Math.max(SIDEBAR_MIN_WIDTH, Math.min(480, clientX));
      setSidebarWidth(newWidth);
    },
    [isResizing],
  );
  const handleResizeEnd = useCallback(() => {
    setIsResizing(false);
  }, []);
  useEffect(() => {
    if (isDragging) {
      window.addEventListener("mousemove", handleDragMove);
      window.addEventListener("mouseup", handleDragEnd);
      window.addEventListener("touchmove", handleDragMove, { passive: false });
      window.addEventListener("touchend", handleDragEnd);
      return () => {
        window.removeEventListener("mousemove", handleDragMove);
        window.removeEventListener("mouseup", handleDragEnd);
        window.removeEventListener("touchmove", handleDragMove);
        window.removeEventListener("touchend", handleDragEnd);
      };
    }
  }, [isDragging, handleDragMove, handleDragEnd]);
  useEffect(() => {
    if (isResizing) {
      document.body.style.cursor = "col-resize";
      document.body.style.userSelect = "none";
      window.addEventListener("mousemove", handleResizeMove);
      window.addEventListener("mouseup", handleResizeEnd);
      window.addEventListener("touchmove", handleResizeMove, {
        passive: false,
      });
      window.addEventListener("touchend", handleResizeEnd);
      return () => {
        document.body.style.cursor = "";
        document.body.style.userSelect = "";
        window.removeEventListener("mousemove", handleResizeMove);
        window.removeEventListener("mouseup", handleResizeEnd);
        window.removeEventListener("touchmove", handleResizeMove);
        window.removeEventListener("touchend", handleResizeEnd);
      };
    }
  }, [isResizing, handleResizeMove, handleResizeEnd]);
  return (
    <>
       
      <div className="hidden h-screen lg:block">
         
        <SidebarContent
          collapsed={collapsed}
          onToggle={() => setCollapsed((value) => !value)}
          width={sidebarWidth}
          onResizeStart={handleResizeStart}
        /> 
      </div> 
      <button
        ref={buttonRef}
        onClick={() => !isDragging && setMobileOpen(true)}
        onMouseDown={handleDragStart}
        onTouchStart={handleDragStart}
        className={`fixed z-40 rounded-xl border border-surface-200 dark:border-surface-700 bg-white/90 dark:bg-surface-800/90 p-2.5 text-surface-700 dark:text-surface-200 shadow-xl backdrop-blur transition-all lg:hidden tap-highlight-transparent ${isDragging ? "cursor-grabbing scale-110 ring-2 ring-primary-500/30" : "cursor-grab hover:shadow-2xl"}`}
        style={{
          left: position.x || undefined,
          top: position.y || undefined,
          ...(position.x === 0 && position.y === 0
            ? { left: "0.75rem", top: "0.75rem" }
            : {}),
        }}
        aria-label="Open sidebar"
      >
         
        <Menu className="h-5 w-5" /> 
        <GripVertical className="absolute -right-1 -top-1 h-3 w-3 text-surface-400 opacity-0 transition-opacity group-hover:opacity-100" /> 
      </button> 
      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
           
          <div
            className="absolute inset-0 bg-slate-950/60 backdrop-blur-sm animate-fade-in"
            onClick={() => setMobileOpen(false)}
            aria-label="Close sidebar overlay"
          /> 
          <div className="relative h-full w-[85vw] max-w-sm animate-slide-right">
             
            <SidebarContent
              collapsed={false}
              onToggle={() => setMobileOpen(false)}
              onNavigate={() => setMobileOpen(false)}
              onClose={() => setMobileOpen(false)}
              width={SIDEBAR_EXPANDED_WIDTH}
            /> 
          </div> 
        </div>
      )} 
    </>
  );
}
