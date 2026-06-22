import { lazy, Suspense } from "react";
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  useParams,
  useLocation,
} from "react-router-dom";
import Register from "./features/auth/pages/Register";
import Login from "./features/auth/pages/Login";
import ForgotPassword from "./features/auth/pages/ForgotPassword";
import ResetPassword from "./features/auth/pages/ResetPassword";
import OAuthSuccess from "./features/auth/pages/OAuthSuccess";
import RegistrationSuccess from "./features/auth/pages/RegistrationSuccess";
import Profile from "./features/auth/pages/Profile";
import Home from "./pages/Home";
import UserHome from "./features/analytics/pages/UserHome";
import InvitationAccept from "./features/auth/pages/InvitationAccept";
import VerifyEmail from "./features/auth/pages/VerifyEmail";
import TwoFactorSetup from "./features/auth/pages/TwoFactorSetup";
import Terms from "./pages/Terms";
import PrivacyPolicy from "./pages/PrivacyPolicy";
import Features from "./pages/Features";
import Manage from "./features/settings/pages/Manage";
import ContactUs from "./pages/ContactUs";
import NotFound from "./pages/NotFound";
import Forbidden from "./features/auth/pages/Forbidden";
import ProtectedRoute from "./features/auth/components/ProtectedRoute";
import WorkspaceOwnerRoute from "./features/auth/components/WorkspaceOwnerRoute";
import { WorkspaceProvider } from "./context/WorkspaceContext";
import WorkspaceLayout from "./layouts/WorkspaceLayout";
import { PageSkeleton } from "./components/LoadingSkeleton";
import {
  MEMBER_VIEW,
  ROLE_ASSIGN_SYSTEM,
  PROJECT_EDIT,
  PROJECT_VIEW,
  PROJECT_VIEW_ALL,
  PROJECT_CREATE,
  TASK_VIEW,
  TASK_VIEW_ALL,
  TASK_CREATE,
  TIMESHEET_APPROVE,
  USER_VIEW,
  ROLE_VIEW,
  WORKFLOW_MANAGE,
  DASHBOARD_VIEW,
  EMPLOYEE_VIEW,
  DEPARTMENT_VIEW,
  DESIGNATION_VIEW,
  ATTENDANCE_VIEW,
  LEAVE_VIEW,
  LEAVE_APPROVE,
  CRM_VIEW,
  LEAD_VIEW,
  CUSTOMER_VIEW,
  CONTACT_VIEW,
  DEAL_VIEW,
  PIPELINE_VIEW,
  FINANCE_VIEW,
} from "./constants/permissions";

const Projects = lazy(() => import("./features/projects/pages/Projects"));
const Tasks = lazy(() => import("./features/tasks/pages/Tasks"));
const Users = lazy(() => import("./features/system/pages/Users"));
const Roles = lazy(() => import("./features/system/pages/Roles"));
const ProjectDetails = lazy(() => import("./features/projects/pages/ProjectDetails"));
const ProjectMembers = lazy(() => import("./features/projects/pages/ProjectMembers"));
const WorkflowStatuses = lazy(() => import("./features/projects/pages/WorkflowStatuses"));
const Board = lazy(() => import("./features/projects/pages/Board"));
const Sprints = lazy(() => import("./features/sprints/pages/Sprints"));
const Analytics = lazy(() => import("./features/analytics/pages/Analytics"));
const CalendarPage = lazy(() => import("./features/tasks/pages/CalendarPage"));
const GanttPage = lazy(() => import("./features/tasks/pages/GanttPage"));
const Timesheet = lazy(() => import("./features/hr/pages/Timesheet"));
const TimesheetApprovals = lazy(() => import("./features/hr/pages/TimesheetApprovals"));
const Webhooks = lazy(() => import("./features/integrations/pages/Webhooks"));
const SlackIntegration = lazy(() => import("./features/integrations/pages/SlackIntegration"));
const ProjectSettings = lazy(() => import("./features/projects/pages/ProjectSettings"));
const ProjectInvitations = lazy(() => import("./features/projects/pages/ProjectInvitations"));
const Notifications = lazy(() => import("./features/notifications/pages/Notifications"));
const AuditLogs = lazy(() => import("./features/system/pages/AuditLogs"));
const SystemHealth = lazy(() => import("./features/system/pages/SystemHealth"));
const JobDashboard = lazy(() => import("./features/analytics/pages/JobDashboard"));
const Settings = lazy(() => import("./features/settings/pages/Settings"));
const SystemSettings = lazy(() => import("./features/settings/pages/SystemSettings"));
const Workspaces = lazy(() => import("./features/settings/pages/Workspaces"));
const WorkspaceMembers = lazy(() => import("./features/settings/pages/WorkspaceMembers"));
const WorkspaceInvitations = lazy(() => import("./features/settings/pages/WorkspaceInvitations"));
const WorkspaceSettings = lazy(() => import("./features/settings/pages/WorkspaceSettings"));
const WorkspaceInvitationAccept = lazy(
  () => import("./features/auth/pages/WorkspaceInvitationAccept"),
);
const WorkspaceDashboard = lazy(() => import("./features/settings/pages/WorkspaceDashboard"));
const MemberDirectory = lazy(() => import("./features/settings/pages/MemberDirectory"));
const WorkspaceActivity = lazy(() => import("./features/settings/pages/WorkspaceActivity"));
const AutomationRules = lazy(() => import("./features/integrations/pages/AutomationRules"));
const ExecutiveInsights = lazy(() => import("./features/analytics/pages/ExecutiveInsights"));
const KnowledgeHub = lazy(() => import("./features/settings/pages/KnowledgeHub"));
const WorkspaceNotes = lazy(() => import("./features/settings/pages/WorkspaceNotes"));
const CreateTaskPage = lazy(() => import("./features/tasks/pages/CreateTaskPage"));
const CreateProjectPage = lazy(() => import("./features/projects/pages/CreateProjectPage"));
const EmployeesPage = lazy(() => import("./features/hr/pages/EmployeesPage"));
const DepartmentsPage = lazy(() => import("./features/hr/pages/DepartmentsPage"));
const DesignationsPage = lazy(() => import("./features/hr/pages/DesignationsPage"));
const AttendancePage = lazy(() => import("./features/hr/pages/AttendancePage"));
const ShiftsPage = lazy(() => import("./features/hr/pages/ShiftsPage"));
const HolidaysPage = lazy(() => import("./features/hr/pages/HolidaysPage"));
const LeaveTypesPage = lazy(() => import("./features/hr/pages/LeaveTypesPage"));
const LeaveRequestsPage = lazy(() => import("./features/hr/pages/LeaveRequestsPage"));
const LeaveApprovalsPage = lazy(() => import("./features/hr/pages/LeaveApprovalsPage"));
const MyProfilePage = lazy(() => import("./features/hr/pages/MyProfilePage"));
const MyAttendancePage = lazy(() => import("./features/hr/pages/MyAttendancePage"));
const MyLeavesPage = lazy(() => import("./features/hr/pages/MyLeavesPage"));
const MyDocumentsPage = lazy(() => import("./features/hr/pages/MyDocumentsPage"));
const HRAnalyticsPage = lazy(() => import("./features/hr/pages/HRAnalyticsPage"));
const UtilizationReportPage = lazy(() => import("./features/hr/pages/UtilizationReportPage"));
const TaskDetailPage = lazy(() => import("./features/tasks/pages/TaskDetailPage"));
const SuperAdminDashboard = lazy(() => import("./features/system/pages/SuperAdminDashboard"));
const EmployeeDashboard = lazy(() => import("./features/hr/pages/EmployeeDashboard"));
const CrmDashboard = lazy(() => import("./features/crm/pages/CrmDashboard"));
const LeadsPage = lazy(() => import("./features/crm/pages/LeadsPage"));
const CustomersPage = lazy(() => import("./features/crm/pages/CustomersPage"));
const ContactsPage = lazy(() => import("./features/crm/pages/ContactsPage"));
const OpportunitiesPage = lazy(() => import("./features/crm/pages/OpportunitiesPage"));
const PipelinePage = lazy(() => import("./features/crm/pages/PipelinePage"));
const LeadDetailPage = lazy(() => import("./features/crm/pages/LeadDetailPage"));
const CustomerDetailPage = lazy(() => import("./features/crm/pages/CustomerDetailPage"));
const FinanceDashboard = lazy(() => import("./features/finance/pages/FinanceDashboard"));
const InvoicesPage = lazy(() => import("./features/finance/pages/InvoicesPage"));
const InvoiceDetailPage = lazy(() => import("./features/finance/pages/InvoiceDetailPage"));
const PaymentsPage = lazy(() => import("./features/finance/pages/PaymentsPage"));
const ExpensesPage = lazy(() => import("./features/finance/pages/ExpensesPage"));

function WorkspaceRoute({ children }) {
  return (
    <ProtectedRoute>
      <WorkspaceProvider>{children}</WorkspaceProvider>
    </ProtectedRoute>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<PageSkeleton />}>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route
            path="/registration-success"
            element={<RegistrationSuccess />}
          />
          <Route path="/verify-email" element={<VerifyEmail />} />
          <Route path="/invite/:token" element={<InvitationAccept />} />
          <Route
            path="/workspace-invite/:token"
            element={<WorkspaceInvitationAccept />}
          />
          <Route
            path="/2fa-setup"
            element={
              <ProtectedRoute>
                <TwoFactorSetup />
              </ProtectedRoute>
            }
          />
          <Route path="/terms" element={<Terms />} />
          <Route path="/privacy" element={<PrivacyPolicy />} />
          <Route path="/features" element={<Features />} />
          <Route path="/manage" element={<Manage />} />
          <Route path="/contact-us" element={<ContactUs />} />

          {/* Workspace-Scoped Routes */}
          <Route
            path="/w/:workspaceSlug"
            element={
              <WorkspaceRoute>
                <WorkspaceLayout />
              </WorkspaceRoute>
            }
          >
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route
              path="dashboard"
              element={
                <ProtectedRoute permission={DASHBOARD_VIEW}>
                  <WorkspaceDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="projects"
              element={
                <ProtectedRoute permissions={[PROJECT_VIEW, PROJECT_VIEW_ALL]}>
                  <Projects />
                </ProtectedRoute>
              }
            />
            <Route
              path="projects/:id"
              element={
                <ProtectedRoute permissions={[PROJECT_VIEW, PROJECT_VIEW_ALL]}>
                  <ProjectDetails />
                </ProtectedRoute>
              }
            />
            <Route
              path="projects/:projectId/board"
              element={
                <ProtectedRoute permissions={[PROJECT_VIEW, PROJECT_VIEW_ALL]}>
                  <Board />
                </ProtectedRoute>
              }
            />
            <Route
              path="projects/:projectId/gantt"
              element={
                <ProtectedRoute permissions={[PROJECT_VIEW, PROJECT_VIEW_ALL]}>
                  <GanttPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="projects/:projectId/invitations"
              element={
                <ProtectedRoute permission={MEMBER_VIEW}>
                  <ProjectInvitations />
                </ProtectedRoute>
              }
            />
            <Route
              path="projects/:projectId/webhooks"
              element={
                <ProtectedRoute permission={ROLE_ASSIGN_SYSTEM}>
                  <Webhooks />
                </ProtectedRoute>
              }
            />
            <Route
              path="projects/:projectId/slack"
              element={
                <ProtectedRoute permission={ROLE_ASSIGN_SYSTEM}>
                  <SlackIntegration />
                </ProtectedRoute>
              }
            />
            <Route
              path="projects/:projectId/settings"
              element={
                <ProtectedRoute permission={PROJECT_EDIT}>
                  <ProjectSettings />
                </ProtectedRoute>
              }
            />
            <Route
              path="tasks"
              element={
                <ProtectedRoute permissions={[TASK_VIEW, TASK_VIEW_ALL]}>
                  <Tasks />
                </ProtectedRoute>
              }
            />
            <Route
              path="tasks/:taskId"
              element={
                <ProtectedRoute permissions={[TASK_VIEW, TASK_VIEW_ALL]}>
                  <TaskDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="create-task"
              element={
                <ProtectedRoute permission={TASK_CREATE}>
                  <CreateTaskPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="create-project"
              element={
                <ProtectedRoute permission={PROJECT_CREATE}>
                  <CreateProjectPage />
                </ProtectedRoute>
              }
            />
            <Route path="activity" element={<WorkspaceActivity />} />
            <Route
              path="analytics"
              element={
                <ProtectedRoute permission={DASHBOARD_VIEW}>
                  <Analytics />
                </ProtectedRoute>
              }
            />
            <Route
              path="insights"
              element={
                <ProtectedRoute permission={DASHBOARD_VIEW}>
                  <ExecutiveInsights />
                </ProtectedRoute>
              }
            />
            <Route path="knowledge" element={<KnowledgeHub />} />
            <Route path="notes" element={<WorkspaceNotes />} />
            <Route
              path="members"
              element={
                <ProtectedRoute permission={MEMBER_VIEW}>
                  <MemberDirectory />
                </ProtectedRoute>
              }
            />
            <Route
              path="settings"
              element={
                <ProtectedRoute requiresAdmin>
                  <WorkspaceSettings />
                </ProtectedRoute>
              }
            />
            <Route
              path="sprints"
              element={
                <ProtectedRoute permission={WORKFLOW_MANAGE}>
                  <Sprints />
                </ProtectedRoute>
              }
            />
            <Route
              path="calendar"
              element={
                <ProtectedRoute permissions={[TASK_VIEW, TASK_VIEW_ALL]}>
                  <CalendarPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="gantt"
              element={
                <ProtectedRoute permissions={[TASK_VIEW, TASK_VIEW_ALL]}>
                  <GanttPage />
                </ProtectedRoute>
              }
            />
            <Route path="timesheet" element={<Timesheet />} />
            <Route
              path="timesheet-approvals"
              element={
                <ProtectedRoute permission={TIMESHEET_APPROVE}>
                  <TimesheetApprovals />
                </ProtectedRoute>
              }
            />
            <Route
              path="users"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <Users />
                </ProtectedRoute>
              }
            />
            <Route
              path="roles"
              element={
                <ProtectedRoute permission={ROLE_VIEW}>
                  <Roles />
                </ProtectedRoute>
              }
            />
            <Route
              path="project-members"
              element={
                <ProtectedRoute permission={MEMBER_VIEW}>
                  <ProjectMembers />
                </ProtectedRoute>
              }
            />
            <Route
              path="workflow-statuses"
              element={
                <ProtectedRoute permission={WORKFLOW_MANAGE}>
                  <WorkflowStatuses />
                </ProtectedRoute>
              }
            />
            <Route
              path="audit-logs"
              element={
                <ProtectedRoute permission={ROLE_VIEW}>
                  <AuditLogs />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/employees"
              element={
                <ProtectedRoute permission={EMPLOYEE_VIEW}>
                  <EmployeesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/departments"
              element={
                <ProtectedRoute permission={DEPARTMENT_VIEW}>
                  <DepartmentsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/designations"
              element={
                <ProtectedRoute permission={DESIGNATION_VIEW}>
                  <DesignationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/attendance"
              element={
                <ProtectedRoute permission={ATTENDANCE_VIEW}>
                  <AttendancePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/shifts"
              element={
                <ProtectedRoute permission={LEAVE_VIEW}>
                  <ShiftsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/holidays"
              element={
                <ProtectedRoute permission={LEAVE_VIEW}>
                  <HolidaysPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/leave-types"
              element={
                <ProtectedRoute permission={LEAVE_VIEW}>
                  <LeaveTypesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/leave-requests"
              element={
                <ProtectedRoute permission={LEAVE_VIEW}>
                  <LeaveRequestsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/leave-approvals"
              element={
                <ProtectedRoute permission={LEAVE_APPROVE}>
                  <LeaveApprovalsPage />
                </ProtectedRoute>
              }
            />
            <Route path="my-profile" element={<MyProfilePage />} />
            <Route path="my-attendance" element={<MyAttendancePage />} />
            <Route path="my-leaves" element={<MyLeavesPage />} />
            <Route path="my-documents" element={<MyDocumentsPage />} />
            <Route
              path="hr/analytics"
              element={
                <ProtectedRoute permission={EMPLOYEE_VIEW}>
                  <HRAnalyticsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/utilization"
              element={
                <ProtectedRoute permission={EMPLOYEE_VIEW}>
                  <UtilizationReportPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="crm"
              element={
                <ProtectedRoute permission={CRM_VIEW}>
                  <CrmDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="crm/leads"
              element={
                <ProtectedRoute permission={LEAD_VIEW}>
                  <LeadsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="crm/leads/:leadId"
              element={
                <ProtectedRoute permission={LEAD_VIEW}>
                  <LeadDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="crm/customers"
              element={
                <ProtectedRoute permission={CUSTOMER_VIEW}>
                  <CustomersPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="crm/customers/:customerId"
              element={
                <ProtectedRoute permission={CUSTOMER_VIEW}>
                  <CustomerDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="crm/contacts"
              element={
                <ProtectedRoute permission={CONTACT_VIEW}>
                  <ContactsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="crm/opportunities"
              element={
                <ProtectedRoute permission={DEAL_VIEW}>
                  <OpportunitiesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="crm/pipeline"
              element={
                <ProtectedRoute permission={PIPELINE_VIEW}>
                  <PipelinePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="finance"
              element={
                <ProtectedRoute permission={FINANCE_VIEW}>
                  <FinanceDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="finance/invoices"
              element={
                <ProtectedRoute permission={FINANCE_VIEW}>
                  <InvoicesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="finance/invoices/:invoiceId"
              element={
                <ProtectedRoute permission={FINANCE_VIEW}>
                  <InvoiceDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="finance/payments"
              element={
                <ProtectedRoute permission={FINANCE_VIEW}>
                  <PaymentsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="finance/expenses"
              element={
                <ProtectedRoute permission={FINANCE_VIEW}>
                  <ExpensesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="system-health"
              element={
                <ProtectedRoute permission={DASHBOARD_VIEW}>
                  <SystemHealth />
                </ProtectedRoute>
              }
            />
            <Route
              path="job-dashboard"
              element={
                <ProtectedRoute permission={ROLE_ASSIGN_SYSTEM}>
                  <JobDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="automation-rules"
              element={
                <ProtectedRoute permission={WORKFLOW_MANAGE}>
                  <AutomationRules />
                </ProtectedRoute>
              }
            />
          </Route>

          {/* Admin Routes (Super Admin only) */}
          <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute globalRole="SUPER_ADMIN">
                <SuperAdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/workspaces"
            element={
              <ProtectedRoute globalRole="SUPER_ADMIN">
                <div className="p-8 text-center text-slate-500">Workspaces Management - Coming Soon</div>
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/users"
            element={
              <ProtectedRoute globalRole="SUPER_ADMIN">
                <div className="p-8 text-center text-slate-500">Users Management - Coming Soon</div>
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/audit-logs"
            element={
              <ProtectedRoute globalRole="SUPER_ADMIN">
                <AuditLogs />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/settings"
            element={
              <ProtectedRoute globalRole="SUPER_ADMIN">
                <SystemSettings />
              </ProtectedRoute>
            }
          />

          {/* Backward-compatible legacy routes */}
          <Route
            path="/user-home"
            element={
              <ProtectedRoute>
                <UserHome />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <Profile />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <Notifications />
              </ProtectedRoute>
            }
          />
          <Route
            path="/settings"
            element={
              <ProtectedRoute>
                <Settings />
              </ProtectedRoute>
            }
          />
          <Route
            path="/workspaces"
            element={
              <ProtectedRoute>
                <Workspaces />
              </ProtectedRoute>
            }
          />
          <Route
            path="/workspaces/:id/members"
            element={
              <ProtectedRoute>
                <WorkspaceMembers />
              </ProtectedRoute>
            }
          />
          <Route
            path="/workspaces/:id/invitations"
            element={
              <ProtectedRoute>
                <WorkspaceInvitations />
              </ProtectedRoute>
            }
          />
          <Route
            path="/workspaces/:id/settings"
            element={
              <ProtectedRoute>
                <WorkspaceLegacyRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/settings/system"
            element={
              <ProtectedRoute>
                <WorkspaceOwnerRoute>
                  <SystemSettings />
                </WorkspaceOwnerRoute>
              </ProtectedRoute>
            }
          />

          {/* Legacy scoped routes - redirect to workspace routes */}
          <Route
            path="/projects"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/projects/:id"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/projects/:projectId/board"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/tasks"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/sprints"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/analytics"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/calendar"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/timesheet"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/timesheet-approvals"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/users"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/roles"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/project-members"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/workflow-statuses"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/audit-logs"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/system-health"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="/job-dashboard"
            element={
              <ProtectedRoute>
                <WorkspaceRedirect />
              </ProtectedRoute>
            }
          />
          <Route path="/forbidden" element={<Forbidden />} />
          <Route path="/unauthorized" element={<Forbidden />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}

function WorkspaceRedirect() {
  const slug = localStorage.getItem("activeWorkspaceSlug");
  const path = window.location.pathname;
  if (slug) {
    return <Navigate to={`/w/${slug}${path}`} replace />;
  }
  return <Navigate to="/workspaces" replace />;
}

function WorkspaceLegacyRedirect() {
  const { id } = useParams();
  const location = useLocation();
  const suffix =
    location.pathname.replace(/^\/workspaces\/[^/]+/, "") || "/settings";
  const slug = localStorage.getItem("activeWorkspaceSlug");
  const activeId = localStorage.getItem("activeWorkspaceId");
  if (slug && (!activeId || String(activeId) === String(id))) {
    return <Navigate to={`/w/${slug}${suffix}`} replace />;
  }
  return <Navigate to="/workspaces" replace />;
}

export default App;
