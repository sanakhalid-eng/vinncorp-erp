import { Link, useLocation, useParams } from "react-router-dom";
import { ChevronRight, Home } from "lucide-react";

const SEGMENT_LABELS = {
  hr: "HR",
  crm: "CRM",
  projects: "Projects",
  tasks: "Tasks",
  users: "Users",
  roles: "Roles",
  settings: "Settings",
  workspaces: "Workspaces",
  members: "Members",
  invitations: "Invitations",
  dashboard: "Dashboard",
  leads: "Leads",
  customers: "Customers",
  contacts: "Contacts",
  opportunities: "Opportunities",
  pipeline: "Pipeline",
  employees: "Employees",
  departments: "Departments",
  designations: "Designations",
  attendance: "Attendance",
  leaves: "Leaves",
  shifts: "Shifts",
  holidays: "Holidays",
  analytics: "Analytics",
  board: "Board",
  sprints: "Sprints",
  calendar: "Calendar",
  gantt: "Gantt",
  timesheet: "Timesheet",
  approvals: "Approvals",
  webhooks: "Webhooks",
  slack: "Slack",
  audit: "Audit Logs",
  health: "System Health",
  templates: "Templates",
  notes: "Notes",
  documents: "Documents",
};

export default function Breadcrumbs({ className = "" }) {
  const location = useLocation();
  const params = useParams();
  const segments = location.pathname.split("/").filter(Boolean);

  // Skip workspace slug (first segment after /w/)
  const breadcrumbSegments = [];
  let i = 0;

  // Find the workspace slug
  while (i < segments.length && segments[i] !== "w") i++;
  if (i < segments.length) i++; // skip "w"
  if (i < segments.length) i++; // skip workspace slug

  // Build breadcrumbs from remaining segments
  const crumbs = [];
  let pathSoFar = "";

  // Reconstruct from workspace root
  const wsSlug = segments.length > 2 ? segments[2] : null;
  if (wsSlug) {
    crumbs.push({ label: "Home", path: `/w/${wsSlug}` });
    pathSoFar = `/w/${wsSlug}`;
  }

  for (; i < segments.length; i++) {
    const seg = segments[i];
    pathSoFar += `/${seg}`;

    // Check if this segment is a param value (UUID, number, or long string)
    const isParam = /^\d+$/.test(seg) || seg.length > 20 || /^[0-9a-f]{8}-/.test(seg);

    if (isParam) {
      // Try to find a meaningful label from params
      const paramKey = Object.keys(params).find((k) => params[k] === seg);
      if (paramKey) {
        crumbs.push({ label: `#${seg}`, path: pathSoFar, isCurrent: i === segments.length - 1 });
      } else {
        crumbs.push({ label: `#${seg}`, path: pathSoFar, isCurrent: i === segments.length - 1 });
      }
    } else {
      crumbs.push({
        label: SEGMENT_LABELS[seg] || seg.charAt(0).toUpperCase() + seg.slice(1),
        path: pathSoFar,
        isCurrent: i === segments.length - 1,
      });
    }
  }

  if (crumbs.length <= 1) return null;

  return (
    <nav className={`flex items-center gap-1 text-sm ${className}`}>
      {crumbs.map((crumb, idx) => (
        <span key={crumb.path} className="flex items-center gap-1">
          {idx > 0 && <ChevronRight className="h-3.5 w-3.5 text-slate-300" />}
          {crumb.isCurrent ? (
            <span className="font-medium text-slate-700">{crumb.label}</span>
          ) : (
            <Link
              to={crumb.path}
              className="text-slate-500 hover:text-indigo-600 transition-colors"
            >
              {idx === 0 ? <Home className="h-3.5 w-3.5" /> : crumb.label}
            </Link>
          )}
        </span>
      ))}
    </nav>
  );
}
