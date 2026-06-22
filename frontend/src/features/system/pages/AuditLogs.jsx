import { useState, useEffect, useCallback } from "react";
import {
  Search,
  Filter,
  Download,
  FileText,
  Shield,
  AlertTriangle,
  Clock,
  User,
  Activity,
  Eye,
  XCircle,
  CheckCircle,
  Info,
  ArrowRight,
  Code,
  Globe,
  Hash,
  ExternalLink,
} from "lucide-react";
import { toast } from "sonner";
import { getFilteredActivities } from "../../analytics/api/activityApi";
import DataTable from "../../../components/table/DataTable";
import Drawer from "../../../components/ui/Drawer";

const ENTITY_TYPES = [
  { value: "", label: "All Entities" },
  { value: "TASK", label: "Task" },
  { value: "PROJECT", label: "Project" },
  { value: "COMMENT", label: "Comment" },
  { value: "ATTACHMENT", label: "Attachment" },
  { value: "MEMBER", label: "Member" },
  { value: "WORKFLOW_STATUS", label: "Workflow Status" },
  { value: "TIME_LOG", label: "Time Log" },
  { value: "TIMESHEET_APPROVAL", label: "Timesheet Approval" },
  { value: "INVITATION", label: "Invitation" },
  { value: "ROLE", label: "Role" },
  { value: "SYSTEM", label: "System" },
];

const ACTION_TYPES = [
  { value: "", label: "All Actions" },
  { value: "CREATED", label: "Created" },
  { value: "UPDATED", label: "Updated" },
  { value: "DELETED", label: "Deleted" },
  { value: "ASSIGNED", label: "Assigned" },
  { value: "UNASSIGNED", label: "Unassigned" },
  { value: "STATUS_CHANGED", label: "Status Changed" },
  { value: "COMMENT_ADDED", label: "Comment Added" },
  { value: "FILE_UPLOADED", label: "File Uploaded" },
  { value: "MEMBER_ADDED", label: "Member Added" },
  { value: "MEMBER_REMOVED", label: "Member Removed" },
  { value: "PRIORITY_CHANGED", label: "Priority Changed" },
  { value: "DUE_DATE_CHANGED", label: "Due Date Changed" },
  { value: "TIME_LOG_CREATED", label: "Time Log Created" },
  { value: "TIME_LOG_UPDATED", label: "Time Log Updated" },
  { value: "TIME_LOG_DELETED", label: "Time Log Deleted" },
  { value: "TIMER_STARTED", label: "Timer Started" },
  { value: "TIMER_STOPPED", label: "Timer Stopped" },
  { value: "ADMIN_ASSIGNED", label: "Admin Assigned" },
  { value: "ADMIN_REMOVED", label: "Admin Removed" },
  { value: "INVITATION_CREATED", label: "Invitation Created" },
  { value: "INVITATION_ACCEPTED", label: "Invitation Accepted" },
  { value: "INVITATION_REVOKED", label: "Invitation Revoked" },
  { value: "OWNERSHIP_TRANSFERRED", label: "Ownership Transferred" },
  { value: "ROLE_CHANGED", label: "Role Changed" },
  { value: "ROLE_EDIT_BLOCKED", label: "Role Edit Blocked" },
  { value: "ROLE_DELETE_BLOCKED", label: "Role Delete Blocked" },
  { value: "ADMIN_ASSIGNMENT_BLOCKED", label: "Admin Assignment Blocked" },
  { value: "WEBHOOK_BLOCKED", label: "Webhook Blocked" },
  { value: "SECURITY_VALIDATION_FAILED", label: "Security Validation Failed" },
];

const SECURITY_ACTIONS = new Set([
  "ROLE_EDIT_BLOCKED",
  "ROLE_DELETE_BLOCKED",
  "ADMIN_ASSIGNMENT_BLOCKED",
  "OWNERSHIP_TRANSFERRED",
  "WEBHOOK_BLOCKED",
  "SECURITY_VALIDATION_FAILED",
]);

function formatDateTime(dateString) {
  if (!dateString) return "-";
  const d = new Date(dateString);
  return d.toLocaleDateString() + " " + d.toLocaleTimeString();
}

function formatDateTimeFull(dateString) {
  if (!dateString) return "-";
  return new Date(dateString).toLocaleString(undefined, {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

function getActionColor(action) {
  if (!action)
    return "bg-surface-100 text-surface-700 dark:bg-surface-800 dark:text-surface-300";
  if (SECURITY_ACTIONS.has(action))
    return "bg-danger-100 text-danger-700 dark:bg-danger-900/30 dark:text-danger-300";
  if (
    action.includes("DELETED") ||
    action.includes("REMOVED") ||
    action.includes("REVOKED") ||
    action.includes("BLOCKED")
  )
    return "bg-danger-50 text-danger-600 dark:bg-danger-900/20 dark:text-danger-400";
  if (
    action.includes("CREATED") ||
    action.includes("ADDED") ||
    action.includes("STARTED") ||
    action.includes("ACCEPTED")
  )
    return "bg-success-50 text-success-600 dark:bg-success-900/20 dark:text-success-400";
  if (action.includes("UPDATED") || action.includes("CHANGED"))
    return "bg-primary-50 text-primary-600 dark:bg-primary-900/20 dark:text-primary-400";
  return "bg-surface-50 text-surface-600 dark:bg-surface-800 dark:text-surface-400";
}

function formatEntityType(type) {
  if (!type) return "-";
  return type.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatAction(action) {
  if (!action) return "-";
  return action.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

function downloadCSV(activities) {
  const headers = [
    "Timestamp",
    "User",
    "Email",
    "Entity Type",
    "Entity ID",
    "Action",
    "Description",
    "Metadata",
  ];
  const rows = activities.map((a) => [
    a.createdAt || "",
    a.user?.name || "System",
    a.user?.email || "",
    a.entityType || "",
    a.entityId ?? "",
    a.action || "",
    (a.description || "").replace(/"/g, '""'),
    a.metadata ? JSON.stringify(a.metadata).replace(/"/g, '""') : "",
  ]);
  const csv = [headers, ...rows]
    .map((r) => r.map((c) => `"${c}"`).join(","))
    .join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute(
    "download",
    `audit-logs-${new Date().toISOString().slice(0, 10)}.csv`,
  );
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function DetailRow({ icon: Icon, label, children }) {
  return (
    <div className="flex gap-3">
      <div className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center">
        {Icon && <Icon className="h-4 w-4 text-surface-400" />}
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-xs font-medium text-surface-500 dark:text-surface-400">{label}</p>
        <div className="mt-0.5 text-sm text-surface-900 dark:text-surface-100">{children}</div>
      </div>
    </div>
  );
}

function MetadataView({ data }) {
  if (!data) return <span className="text-surface-400">-</span>;
  const entries = Object.entries(data);
  if (entries.length === 0) return <span className="text-surface-400">-</span>;
  return (
    <div className="space-y-1">
      {entries.map(([key, value]) => (
        <div key={key} className="flex gap-2 text-xs">
          <span className="shrink-0 font-medium text-surface-500">{key}:</span>
          <span className="text-surface-700 dark:text-surface-300 break-all">
            {typeof value === "object" ? JSON.stringify(value) : String(value)}
          </span>
        </div>
      ))}
    </div>
  );
}

function ValueDiff({ oldValue, newValue }) {
  if (oldValue == null && newValue == null) return null;
  return (
    <div className="space-y-2">
      {oldValue != null && (
        <div className="rounded-lg border border-danger-200 bg-danger-50/50 p-3 dark:border-danger-900/30 dark:bg-danger-900/10">
          <div className="mb-1 flex items-center gap-1.5 text-xs font-medium text-danger-600 dark:text-danger-400">
            <XCircle className="h-3.5 w-3.5" />
            Old Value
          </div>
          <pre className="whitespace-pre-wrap text-xs text-danger-700 dark:text-danger-300">
            {typeof oldValue === "object" ? JSON.stringify(oldValue, null, 2) : String(oldValue)}
          </pre>
        </div>
      )}
      {newValue != null && (
        <div className="rounded-lg border border-success-200 bg-success-50/50 p-3 dark:border-success-900/30 dark:bg-success-900/10">
          <div className="mb-1 flex items-center gap-1.5 text-xs font-medium text-success-600 dark:text-success-400">
            <CheckCircle className="h-3.5 w-3.5" />
            New Value
          </div>
          <pre className="whitespace-pre-wrap text-xs text-success-700 dark:text-success-300">
            {typeof newValue === "object" ? JSON.stringify(newValue, null, 2) : String(newValue)}
          </pre>
        </div>
      )}
    </div>
  );
}

export default function AuditLogs() {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 20;
  const [search, setSearch] = useState("");
  const [entityType, setEntityType] = useState("");
  const [action, setAction] = useState("");
  const [securityOnly, setSecurityOnly] = useState(false);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [showFilters, setShowFilters] = useState(false);
  const [selectedActivity, setSelectedActivity] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const fetchActivities = useCallback(
    async (pageNum = 0) => {
      setLoading(true);
      try {
        const params = { page: pageNum, size: pageSize, securityOnly };
        if (search.trim()) params.search = search.trim();
        if (entityType) params.entityType = entityType;
        if (action) params.action = action;
        if (startDate) params.startDate = new Date(startDate).toISOString();
        if (endDate)
          params.endDate = new Date(endDate + "T23:59:59").toISOString();
        const result = await getFilteredActivities(params);
        const data = result?.data ?? {};
        setActivities(data?.content ?? []);
        setPage(data?.page ?? 0);
        setTotalPages(data?.totalPages ?? 0);
        setTotalElements(data?.totalElements ?? 0);
      } catch {
        toast.error("Failed to load audit logs");
        setActivities([]);
      } finally {
        setLoading(false);
      }
    },
    [search, entityType, action, securityOnly, startDate, endDate],
  );

  useEffect(() => {
    fetchActivities(0);
  }, [fetchActivities]);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchActivities(newPage);
    }
  };

  const handleExportCSV = () => {
    if (activities.length === 0) {
      toast.error("No data to export");
      return;
    }
    downloadCSV(activities);
    toast.success("CSV downloaded");
  };

  const handleExportPDF = () => {
    toast.info("PDF export will be available soon");
  };

  const handleRowClick = (activity) => {
    setSelectedActivity(activity);
    setDrawerOpen(true);
  };

  const columns = [
    {
      header: "Timestamp",
      accessor: "createdAt",
      width: "180px",
      sortValue: (row) => (row.createdAt ? new Date(row.createdAt).getTime() : 0),
      render: (row) => (
        <div className="flex items-center gap-1.5 whitespace-nowrap text-surface-500 dark:text-surface-400">
          <Clock className="h-3.5 w-3.5 text-surface-400" />
          {formatDateTime(row.createdAt)}
        </div>
      ),
    },
    {
      header: "User",
      accessor: "user",
      sortValue: (row) => row.user?.name ?? "",
      render: (row) =>
        row.user ? (
          <div className="flex items-center gap-2">
            {row.user.avatarUrl ? (
              <img
                src={row.user.avatarUrl}
                alt=""
                className="h-7 w-7 rounded-full object-cover"
              />
            ) : (
              <div className="flex h-7 w-7 items-center justify-center rounded-full bg-primary-100 dark:bg-primary-900/30 text-xs font-medium text-primary-600 dark:text-primary-400">
                {(row.user.name || "?")[0]}
              </div>
            )}
            <div>
              <p className="font-medium text-surface-900 dark:text-surface-100">
                {row.user.name}
              </p>
              <p className="text-xs text-surface-400">{row.user.email}</p>
            </div>
          </div>
        ) : (
          <div className="flex items-center gap-1.5 text-surface-400">
            <User className="h-4 w-4" />
            <span>System</span>
          </div>
        ),
    },
    {
      header: "Entity",
      accessor: "entityType",
      width: "160px",
      sortValue: (row) => row.entityType ?? "",
      render: (row) => (
        <div className="flex items-center gap-1.5">
          <span className="rounded-md bg-surface-100 px-2 py-0.5 text-xs font-medium text-surface-600 dark:bg-surface-800 dark:text-surface-400">
            {formatEntityType(row.entityType)}
          </span>
          {row.entityId != null && (
            <span className="text-xs text-surface-400">#{row.entityId}</span>
          )}
        </div>
      ),
    },
    {
      header: "Action",
      accessor: "action",
      width: "180px",
      sortValue: (row) => row.action ?? "",
      render: (row) => {
        const isSecurity = SECURITY_ACTIONS.has(row.action);
        return (
          <div className="flex items-center gap-1.5">
            {isSecurity && (
              <AlertTriangle className="h-3.5 w-3.5 text-danger-500" />
            )}
            <span
              className={`rounded-md px-2 py-0.5 text-xs font-medium ${getActionColor(row.action)}`}
            >
              {formatAction(row.action)}
            </span>
          </div>
        );
      },
    },
    {
      header: "Description",
      accessor: "description",
      render: (row) => (
        <div className="flex items-center gap-2">
          <span className="max-w-xs truncate text-surface-600 dark:text-surface-400">
            {row.description || "-"}
          </span>
          <Eye className="ml-auto h-3.5 w-3.5 shrink-0 text-surface-300 dark:text-surface-600" />
        </div>
      ),
    },
  ];

  return (
    <div className="page-container max-w-8xl mx-auto">
      <div className="page-header">
        <div>
          <h1 className="page-title">Audit Logs</h1>
          <p className="page-subtitle">
            {totalElements} event{totalElements !== 1 ? "s" : ""} recorded
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowFilters(!showFilters)}
            className={`btn btn-sm ${showFilters ? "bg-primary-100 text-primary-600 dark:bg-primary-900/30 dark:text-primary-400" : "btn-secondary"}`}
          >
            <Filter className="h-4 w-4" /> Filters
          </button>
          <button
            onClick={handleExportCSV}
            className="btn btn-sm btn-secondary"
          >
            <Download className="h-4 w-4" /> CSV
          </button>
          <button
            onClick={handleExportPDF}
            className="btn btn-sm btn-secondary"
          >
            <FileText className="h-4 w-4" /> PDF
          </button>
        </div>
      </div>

      {showFilters && (
        <div className="mb-4 rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-900 p-4 shadow-soft">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <label className="mb-1 block text-xs font-medium text-surface-500 dark:text-surface-400">
                Search Metadata
              </label>
              <div className="relative">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-surface-400" />
                <input
                  type="text"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="Search metadata..."
                  className="input-field pl-8"
                />
              </div>
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-surface-500 dark:text-surface-400">
                Entity Type
              </label>
              <select
                value={entityType}
                onChange={(e) => setEntityType(e.target.value)}
                className="input-field"
              >
                {ENTITY_TYPES.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-surface-500 dark:text-surface-400">
                Action
              </label>
              <select
                value={action}
                onChange={(e) => setAction(e.target.value)}
                className="input-field"
              >
                {ACTION_TYPES.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex items-end">
              <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-surface-200 dark:border-surface-700 px-3 py-2 text-sm">
                <input
                  type="checkbox"
                  checked={securityOnly}
                  onChange={(e) => setSecurityOnly(e.target.checked)}
                  className="h-4 w-4 rounded border-surface-300 dark:border-surface-600 text-primary-600 focus:ring-primary-500"
                />
                <Shield className="h-4 w-4 text-danger-500" />
                <span className="font-medium text-surface-700 dark:text-surface-300">
                  Security Events Only
                </span>
              </label>
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-surface-500 dark:text-surface-400">
                Start Date
              </label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="input-field"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-surface-500 dark:text-surface-400">
                End Date
              </label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="input-field"
              />
            </div>
          </div>
        </div>
      )}

      <DataTable
        columns={columns}
        data={activities}
        loading={loading}
        searchable={false}
        emptyMessage="No audit logs found"
        emptyIcon={Activity}
        serverSide
        totalPages={totalPages}
        currentPage={page}
        onPageChange={handlePageChange}
        totalRecords={totalElements}
        pageSize={pageSize}
        onRowClick={handleRowClick}
        striped
      />

      <Drawer
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        title="Activity Details"
        size="lg"
        position="right"
      >
        {selectedActivity && (
          <div className="space-y-6">
            <div className="flex items-center gap-3 rounded-xl border border-surface-200 bg-surface-50/50 p-4 dark:border-surface-700 dark:bg-surface-800/30">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary-100 dark:bg-primary-900/30">
                <Activity className="h-5 w-5 text-primary-600 dark:text-primary-400" />
              </div>
              <div>
                <p className="text-sm font-medium text-surface-900 dark:text-surface-100">
                  {selectedActionLabel(selectedActivity.action)}
                </p>
                <p className="text-xs text-surface-400">ID: {selectedActivity.id}</p>
              </div>
            </div>

            <div className="space-y-4">
              <DetailRow icon={Clock} label="Timestamp">
                <span className="font-medium">{formatDateTimeFull(selectedActivity.createdAt)}</span>
              </DetailRow>

              <DetailRow icon={User} label="User">
                {selectedActivity.user ? (
                  <div className="flex items-center gap-2">
                    {selectedActivity.user.avatarUrl ? (
                      <img
                        src={selectedActivity.user.avatarUrl}
                        alt=""
                        className="h-8 w-8 rounded-full object-cover"
                      />
                    ) : (
                      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 dark:bg-primary-900/30 text-sm font-medium text-primary-600 dark:text-primary-400">
                        {(selectedActivity.user.name || "?")[0]}
                      </div>
                    )}
                    <div>
                      <p className="font-medium text-surface-900 dark:text-surface-100">
                        {selectedActivity.user.name}
                      </p>
                      <p className="text-xs text-surface-400">{selectedActivity.user.email}</p>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center gap-1.5 text-surface-400">
                    <User className="h-4 w-4" />
                    <span>System</span>
                  </div>
                )}
              </DetailRow>

              <DetailRow icon={Hash} label="Entity">
                <div className="flex items-center gap-2">
                  <span
                    className={`rounded-md px-2 py-0.5 text-xs font-medium ${getActionColor(selectedActivity.action)}`}
                  >
                    {formatEntityType(selectedActivity.entityType)}
                  </span>
                  {selectedActivity.entityId != null && (
                    <span className="text-sm text-surface-500">
                      #{selectedActivity.entityId}
                    </span>
                  )}
                </div>
              </DetailRow>

              <DetailRow icon={Info} label="Action">
                <div className="flex items-center gap-2">
                  {SECURITY_ACTIONS.has(selectedActivity.action) && (
                    <AlertTriangle className="h-4 w-4 text-danger-500" />
                  )}
                  <span
                    className={`rounded-md px-2 py-0.5 text-xs font-medium ${getActionColor(selectedActivity.action)}`}
                  >
                    {formatAction(selectedActivity.action)}
                  </span>
                </div>
              </DetailRow>

              <DetailRow icon={ExternalLink} label="Description">
                <p className="text-sm text-surface-700 dark:text-surface-300">
                  {selectedActivity.description || "-"}
                </p>
              </DetailRow>

              {(selectedActivity.oldValue != null || selectedActivity.newValue != null) && (
                <div className="rounded-xl border border-surface-200 p-4 dark:border-surface-700">
                  <div className="mb-3 flex items-center gap-2">
                    <ArrowRight className="h-4 w-4 text-surface-400" />
                    <span className="text-xs font-semibold uppercase tracking-wider text-surface-500">
                      Value Changes
                    </span>
                  </div>
                  <ValueDiff
                    oldValue={selectedActivity.oldValue}
                    newValue={selectedActivity.newValue}
                  />
                </div>
              )}

              <div className="rounded-xl border border-surface-200 p-4 dark:border-surface-700">
                <div className="mb-3 flex items-center gap-2">
                  <Code className="h-4 w-4 text-surface-400" />
                  <span className="text-xs font-semibold uppercase tracking-wider text-surface-500">
                    Metadata
                  </span>
                </div>
                <MetadataView data={selectedActivity.metadata} />
              </div>

              {selectedActivity.workspaceId && (
                <DetailRow icon={Globe} label="Workspace">
                  <span className="text-sm text-surface-700 dark:text-surface-300">
                    {selectedActivity.workspaceId}
                  </span>
                </DetailRow>
              )}
            </div>
          </div>
        )}
      </Drawer>
    </div>
  );
}

function selectedActionLabel(action) {
  if (!action) return "Unknown Action";
  return action.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}
