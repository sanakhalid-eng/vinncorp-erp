import { lazy, Suspense } from "react";
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  useParams,
  useLocation,
} from "react-router-dom";
import Register from "./pages/Register";
import Login from "./pages/Login";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import OAuthSuccess from "./pages/OAuthSuccess";
import RegistrationSuccess from "./pages/RegistrationSuccess";
import Profile from "./pages/Profile";
import Home from "./pages/Home";
import UserHome from "./pages/UserHome";
import InvitationAccept from "./pages/InvitationAccept";
import VerifyEmail from "./pages/VerifyEmail";
import TwoFactorSetup from "./pages/TwoFactorSetup";
import Terms from "./pages/Terms";
import PrivacyPolicy from "./pages/PrivacyPolicy";
import Features from "./pages/Features";
import Manage from "./pages/Manage";
import ContactUs from "./pages/ContactUs";
import NotFound from "./pages/NotFound";
import Forbidden from "./pages/Forbidden";
import ProtectedRoute from "./components/ProtectedRoute";
import WorkspaceOwnerRoute from "./components/WorkspaceOwnerRoute";
import { WorkspaceProvider } from "./context/WorkspaceContext";
import WorkspaceLayout from "./layouts/WorkspaceLayout";
import { PageSkeleton } from "./components/LoadingSkeleton";
import {
  MEMBER_VIEW,
  ROLE_ASSIGN_SYSTEM,
  PROJECT_EDIT,
  TIMESHEET_APPROVE,
  USER_VIEW,
  ROLE_VIEW,
  WORKFLOW_MANAGE,
  DASHBOARD_VIEW,
} from "./constants/permissions";

const Projects = lazy(() => import("./pages/Projects"));
const Tasks = lazy(() => import("./pages/Tasks"));
const Users = lazy(() => import("./pages/Users"));
const Roles = lazy(() => import("./pages/Roles"));
const ProjectDetails = lazy(() => import("./pages/ProjectDetails"));
const ProjectMembers = lazy(() => import("./pages/ProjectMembers"));
const WorkflowStatuses = lazy(() => import("./pages/WorkflowStatuses"));
const Board = lazy(() => import("./pages/Board"));
const Sprints = lazy(() => import("./pages/Sprints"));
const Analytics = lazy(() => import("./pages/Analytics"));
const CalendarPage = lazy(() => import("./pages/CalendarPage"));
const GanttPage = lazy(() => import("./pages/GanttPage"));
const Timesheet = lazy(() => import("./pages/Timesheet"));
const TimesheetApprovals = lazy(() => import("./pages/TimesheetApprovals"));
const Webhooks = lazy(() => import("./pages/Webhooks"));
const SlackIntegration = lazy(() => import("./pages/SlackIntegration"));
const ProjectSettings = lazy(() => import("./pages/ProjectSettings"));
const ProjectInvitations = lazy(() => import("./pages/ProjectInvitations"));
const Notifications = lazy(() => import("./pages/Notifications"));
const AuditLogs = lazy(() => import("./pages/AuditLogs"));
const SystemHealth = lazy(() => import("./pages/SystemHealth"));
const JobDashboard = lazy(() => import("./pages/JobDashboard"));
const Settings = lazy(() => import("./pages/Settings"));
const SystemSettings = lazy(() => import("./pages/SystemSettings"));
const Workspaces = lazy(() => import("./pages/Workspaces"));
const WorkspaceMembers = lazy(() => import("./pages/WorkspaceMembers"));
const WorkspaceInvitations = lazy(() => import("./pages/WorkspaceInvitations"));
const WorkspaceSettings = lazy(() => import("./pages/WorkspaceSettings"));
const WorkspaceInvitationAccept = lazy(
  () => import("./pages/WorkspaceInvitationAccept"),
);
const WorkspaceDashboard = lazy(() => import("./pages/WorkspaceDashboard"));
const MemberDirectory = lazy(() => import("./pages/MemberDirectory"));
const WorkspaceActivity = lazy(() => import("./pages/WorkspaceActivity"));
const AutomationRules = lazy(() => import("./pages/AutomationRules"));
const ExecutiveInsights = lazy(() => import("./pages/ExecutiveInsights"));
const KnowledgeHub = lazy(() => import("./pages/KnowledgeHub"));
const WorkspaceNotes = lazy(() => import("./pages/WorkspaceNotes"));
const CreateTaskPage = lazy(() => import("./pages/CreateTaskPage"));
const CreateProjectPage = lazy(() => import("./pages/CreateProjectPage"));
const EmployeesPage = lazy(() => import("./modules/hr/pages/EmployeesPage"));
const DepartmentsPage = lazy(() => import("./modules/hr/pages/DepartmentsPage"));
const DesignationsPage = lazy(() => import("./modules/hr/pages/DesignationsPage"));
const AttendancePage = lazy(() => import("./modules/hr/pages/AttendancePage"));
const ShiftsPage = lazy(() => import("./modules/hr/pages/ShiftsPage"));
const HolidaysPage = lazy(() => import("./modules/hr/pages/HolidaysPage"));
const LeaveTypesPage = lazy(() => import("./modules/hr/pages/LeaveTypesPage"));
const LeaveRequestsPage = lazy(() => import("./modules/hr/pages/LeaveRequestsPage"));
const LeaveApprovalsPage = lazy(() => import("./modules/hr/pages/LeaveApprovalsPage"));
const MyProfilePage = lazy(() => import("./modules/hr/pages/MyProfilePage"));
const MyAttendancePage = lazy(() => import("./modules/hr/pages/MyAttendancePage"));
const MyLeavesPage = lazy(() => import("./modules/hr/pages/MyLeavesPage"));
const MyDocumentsPage = lazy(() => import("./modules/hr/pages/MyDocumentsPage"));
const HRAnalyticsPage = lazy(() => import("./modules/hr/pages/HRAnalyticsPage"));
const UtilizationReportPage = lazy(() => import("./modules/hr/pages/UtilizationReportPage"));
const TaskDetailPage = lazy(() => import("./pages/TaskDetailPage"));
const SuperAdminDashboard = lazy(() => import("./pages/SuperAdminDashboard"));
const EmployeeDashboard = lazy(() => import("./pages/EmployeeDashboard"));
const CrmDashboard = lazy(() => import("./modules/crm/pages/CrmDashboard"));
const LeadsPage = lazy(() => import("./modules/crm/pages/LeadsPage"));
const CustomersPage = lazy(() => import("./modules/crm/pages/CustomersPage"));
const ContactsPage = lazy(() => import("./modules/crm/pages/ContactsPage"));
const OpportunitiesPage = lazy(() => import("./modules/crm/pages/OpportunitiesPage"));
const PipelinePage = lazy(() => import("./modules/crm/pages/PipelinePage"));
const LeadDetailPage = lazy(() => import("./modules/crm/pages/LeadDetailPage"));
const CustomerDetailPage = lazy(() => import("./modules/crm/pages/CustomerDetailPage"));

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
            <Route path="dashboard" element={<WorkspaceDashboard />} />
            <Route path="projects" element={<Projects />} />
            <Route path="projects/:id" element={<ProjectDetails />} />
            <Route path="projects/:projectId/board" element={<Board />} />
            <Route path="projects/:projectId/gantt" element={<GanttPage />} />
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
            <Route path="tasks" element={<Tasks />} />
            <Route path="tasks/:taskId" element={<TaskDetailPage />} />
            <Route path="create-task" element={<CreateTaskPage />} />
            <Route path="create-project" element={<CreateProjectPage />} />
            <Route path="activity" element={<WorkspaceActivity />} />
            <Route path="analytics" element={<Analytics />} />
            <Route path="insights" element={<ExecutiveInsights />} />
            <Route path="knowledge" element={<KnowledgeHub />} />
            <Route path="notes" element={<WorkspaceNotes />} />
            <Route path="members" element={<MemberDirectory />} />
            <Route path="settings" element={<WorkspaceSettings />} />
            <Route path="sprints" element={<Sprints />} />
            <Route path="calendar" element={<CalendarPage />} />
            <Route path="gantt" element={<GanttPage />} />
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
            <Route path="audit-logs" element={<AuditLogs />} />
            <Route
              path="hr/employees"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <EmployeesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/departments"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <DepartmentsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/designations"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <DesignationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/attendance"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <AttendancePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/shifts"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <ShiftsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/holidays"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <HolidaysPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/leave-types"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <LeaveTypesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/leave-requests"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <LeaveRequestsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/leave-approvals"
              element={
                <ProtectedRoute permission={USER_VIEW}>
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
                <ProtectedRoute permission={USER_VIEW}>
                  <HRAnalyticsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="hr/utilization"
              element={
                <ProtectedRoute permission={USER_VIEW}>
                  <UtilizationReportPage />
                </ProtectedRoute>
              }
            />
            <Route path="crm" element={<CrmDashboard />} />
            <Route path="crm/leads" element={<LeadsPage />} />
            <Route path="crm/leads/:leadId" element={<LeadDetailPage />} />
            <Route path="crm/customers" element={<CustomersPage />} />
            <Route path="crm/customers/:customerId" element={<CustomerDetailPage />} />
            <Route path="crm/contacts" element={<ContactsPage />} />
            <Route path="crm/opportunities" element={<OpportunitiesPage />} />
            <Route path="crm/pipeline" element={<PipelinePage />} />
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
