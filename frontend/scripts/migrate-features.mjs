import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const SRC = path.join(__dirname, '..', 'src');

const FEATURE_MOVES = {
  auth: {
    pages: [
      'Login.jsx', 'Register.jsx', 'ForgotPassword.jsx', 'ResetPassword.jsx',
      'OAuthSuccess.jsx', 'RegistrationSuccess.jsx', 'VerifyEmail.jsx',
      'TwoFactorSetup.jsx', 'InvitationAccept.jsx', 'WorkspaceInvitationAccept.jsx',
      'Profile.jsx', 'Forbidden.jsx',
    ],
    components: [
      'ProtectedRoute.jsx', 'PermissionRoute.jsx', 'WorkspaceOwnerRoute.jsx',
      'TwoFactorModal.jsx', 'ChangePasswordModal.jsx', 'OnboardingFlow.jsx', 'RoleGuard.jsx',
    ],
    api: ['twoFactorApi.ts', 'invitationApi.ts'],
  },
  finance: {
    api: ['financeApi.ts'],
  },
  hr: {
    pages: ['Timesheet.jsx', 'TimesheetApprovals.jsx', 'EmployeeDashboard.jsx'],
  },
  projects: {
    pages: [
      'Projects.jsx', 'ProjectDetails.jsx', 'ProjectMembers.jsx', 'ProjectSettings.jsx',
      'ProjectInvitations.jsx', 'CreateProjectPage.jsx', 'Board.jsx', 'WorkflowStatuses.jsx',
    ],
    componentDirs: ['projects', 'statuses', 'members', 'labels'],
    components: [
      'ProjectCard.jsx', 'ProjectFormModal.jsx', 'ProjectNavBar.jsx', 'TemplateSelector.jsx',
    ],
    api: [
      'projectApi.ts', 'projectMembersApi.ts', 'projectTemplateApi.ts',
      'boardApi.ts', 'statusApi.ts', 'labelApi.ts',
    ],
  },
  tasks: {
    pages: [
      'Tasks.jsx', 'TaskDetailPage.jsx', 'CreateTaskPage.jsx', 'GanttPage.jsx', 'CalendarPage.jsx',
    ],
    componentDirs: ['tasks', 'subtasks', 'comments'],
    components: ['TimeTrackingSection.jsx', 'attachments/AttachmentList.jsx'],
    hooks: ['useOptimisticMutation.js'],
    api: [
      'taskApi.ts', 'subtaskApi.ts', 'taskDependencyApi.ts', 'commentApi.ts',
      'attachmentApi.ts', 'recurringApi.ts', 'ganttApi.ts', 'calendarApi.ts',
    ],
  },
  sprints: {
    pages: ['Sprints.jsx'],
    componentDirs: ['sprints'],
    api: ['sprintApi.ts'],
  },
  notifications: {
    pages: ['Notifications.jsx'],
    componentDirs: ['notifications'],
    hooks: ['useNotificationWebSocket.js'],
    api: ['notificationApi.ts'],
  },
  analytics: {
    pages: ['Analytics.jsx', 'ExecutiveInsights.jsx', 'JobDashboard.jsx', 'UserHome.jsx'],
    componentDirs: ['dashboard'],
    components: ['StatsSection.jsx', 'StatCard.jsx', 'ActivityFeed.jsx', 'ExportDropdown.jsx'],
    api: ['analyticsApi.ts', 'dashboardApi.ts', 'exportApi.ts', 'activityApi.ts'],
  },
  settings: {
    pages: [
      'Settings.jsx', 'SystemSettings.jsx', 'WorkspaceSettings.jsx', 'Manage.jsx',
      'Workspaces.jsx', 'WorkspaceMembers.jsx', 'WorkspaceInvitations.jsx',
      'WorkspaceDashboard.jsx', 'WorkspaceActivity.jsx', 'WorkspaceNotes.jsx',
      'MemberDirectory.jsx', 'KnowledgeHub.jsx',
    ],
    components: [
      'ThemeToggle.jsx', 'WorkspaceSwitcher.jsx', 'UserFormModal.jsx',
      'RoleFormModal.jsx', 'AssignRoleModal.jsx',
    ],
    api: ['workspaceApi.ts', 'notesApi.ts', 'searchApi.ts', 'permissionApi.ts', 'roleApi.ts'],
  },
  system: {
    pages: [
      'AuditLogs.jsx', 'SystemHealth.jsx', 'Users.jsx', 'Roles.jsx', 'SuperAdminDashboard.jsx',
    ],
    components: ['UserTable.jsx', 'RoleTable.jsx', 'SystemRoleModal.jsx'],
    api: ['systemApi.ts'],
  },
  integrations: {
    pages: ['Webhooks.jsx', 'SlackIntegration.jsx', 'AutomationRules.jsx'],
    componentDirs: ['automation'],
    api: ['automationApi.ts'],
  },
};

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function moveFile(from, to) {
  if (!fs.existsSync(from)) {
    console.warn(`SKIP (missing): ${from}`);
    return false;
  }
  ensureDir(path.dirname(to));
  if (fs.existsSync(to)) {
    console.warn(`SKIP (exists): ${to}`);
    return false;
  }
  fs.renameSync(from, to);
  console.log(`MOVED: ${path.relative(SRC, from)} -> ${path.relative(SRC, to)}`);
  return true;
}

function moveDirContents(fromDir, toDir) {
  if (!fs.existsSync(fromDir)) {
    console.warn(`SKIP dir (missing): ${fromDir}`);
    return;
  }
  ensureDir(toDir);
  for (const entry of fs.readdirSync(fromDir, { withFileTypes: true })) {
    const from = path.join(fromDir, entry.name);
    const to = path.join(toDir, entry.name);
    if (entry.isDirectory()) {
      moveDirContents(from, to);
    } else {
      moveFile(from, to);
    }
  }
  if (fs.existsSync(fromDir) && fs.readdirSync(fromDir).length === 0) {
    fs.rmdirSync(fromDir);
  }
}

function executeMoves() {
  const moved = [];

  for (const [feature, config] of Object.entries(FEATURE_MOVES)) {
    const base = path.join(SRC, 'features', feature);

    for (const page of config.pages || []) {
      const from = path.join(SRC, 'pages', page);
      const to = path.join(base, 'pages', page);
      if (moveFile(from, to)) moved.push({ from: `pages/${page}`, to: `features/${feature}/pages/${page}`, feature });
    }

    for (const comp of config.components || []) {
      const from = path.join(SRC, 'components', comp);
      const to = path.join(base, 'components', comp);
      if (moveFile(from, to)) moved.push({ from: `components/${comp}`, to: `features/${feature}/components/${comp}`, feature });
    }

    for (const dir of config.componentDirs || []) {
      const fromDir = path.join(SRC, 'components', dir);
      const toDir = path.join(base, 'components', dir);
      if (fs.existsSync(fromDir)) {
        moveDirContents(fromDir, toDir);
        moved.push({ from: `components/${dir}/`, to: `features/${feature}/components/${dir}/`, feature });
      }
    }

    for (const hook of config.hooks || []) {
      const from = path.join(SRC, 'hooks', hook);
      const to = path.join(base, 'hooks', hook);
      if (moveFile(from, to)) moved.push({ from: `hooks/${hook}`, to: `features/${feature}/hooks/${hook}`, feature });
    }

    for (const api of config.api || []) {
      const from = path.join(SRC, 'api', api);
      const to = path.join(base, 'api', api);
      if (moveFile(from, to)) moved.push({ from: `api/${api}`, to: `features/${feature}/api/${api}`, feature });
    }
  }

  return moved;
}

const SHARED_SRC_PREFIXES = ['api', 'components', 'context', 'hooks', 'utils', 'lib', 'constants', 'assets', 'layouts', 'types'];

function fixSharedImportsInFeatureFiles() {
  const featuresDir = path.join(SRC, 'features');
  if (!fs.existsSync(featuresDir)) return;

  const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) walk(full);
      else if (/\.(jsx?|tsx?)$/.test(entry.name)) fixFileSharedImports(full);
    }
  };
  walk(featuresDir);
}

function fixFileSharedImports(filePath) {
  let content = fs.readFileSync(filePath, 'utf8');
  let changed = false;

  const replaceImport = (regex, replacement) => {
    const next = content.replace(regex, replacement);
    if (next !== content) {
      content = next;
      changed = true;
    }
  };

  for (const prefix of SHARED_SRC_PREFIXES) {
    // ../prefix/ -> ../../../prefix/  (but not if already ../../../)
    replaceImport(
      new RegExp(`from (["'])\\.\\./${prefix}/`, 'g'),
      `from $1../../../${prefix}/`,
    );
    replaceImport(
      new RegExp(`from (["'])\\.\\./\\.\\./${prefix}/`, 'g'),
      `from $1../../../${prefix}/`,
    );
  }

  // Feature API files importing shared axios
  replaceImport(/from (["'])\.\.\/\.\.\/\.\.\/api\/axios/g, `from $1../../../api/axios`);

  if (changed) {
    fs.writeFileSync(filePath, content);
    console.log(`FIXED shared imports: ${path.relative(SRC, filePath)}`);
  }
}

function buildGlobalReplacements() {
  const replacements = [];

  const add = (from, to) => {
    replacements.push([new RegExp(from.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), to]);
  };

  for (const [feature, config] of Object.entries(FEATURE_MOVES)) {
    for (const page of config.pages || []) {
      const base = page.replace(/\.(jsx|tsx|js|ts)$/, '');
      add(`./pages/${base}`, `./features/${feature}/pages/${base}`);
      add(`../pages/${base}`, `../features/${feature}/pages/${base}`);
      add(`"./pages/${page}"`, `"./features/${feature}/pages/${page}"`);
      add(`'./pages/${page}'`, `'./features/${feature}/pages/${page}'`);
    }

    for (const comp of config.components || []) {
      const compPath = comp.replace(/\\/g, '/');
      const base = compPath.replace(/\.(jsx|tsx|js|ts)$/, '');
      add(`./components/${compPath}`, `./features/${feature}/components/${compPath}`);
      add(`../components/${compPath}`, `../features/${feature}/components/${compPath}`);
    }

    for (const dir of config.componentDirs || []) {
      add(`./components/${dir}/`, `./features/${feature}/components/${dir}/`);
      add(`../components/${dir}/`, `../features/${feature}/components/${dir}/`);
    }

    for (const hook of config.hooks || []) {
      const base = hook.replace(/\.(jsx|tsx|js|ts)$/, '');
      add(`./hooks/${base}`, `./features/hooks/${base}`.replace('/hooks/', `/${feature}/hooks/`));
      add(`../hooks/${hook}`, `../features/${feature}/hooks/${hook}`);
    }

    for (const api of config.api || []) {
      const base = api.replace(/\.(jsx|tsx|js|ts)$/, '');
      add(`./api/${base}`, `./features/${feature}/api/${base}`);
      add(`../api/${base}`, `../features/${feature}/api/${base}`);
      add(`"../api/${api}"`, `"../features/${feature}/api/${api}"`);
      add(`'../api/${api}'`, `'../features/${feature}/api/${api}'`);
    }
  }

  // Cross-feature: task components referenced from legacy paths
  add('../components/tasks/', '../features/tasks/components/tasks/');
  add('./components/tasks/', './features/tasks/components/tasks/');
  add('../components/subtasks/', '../features/tasks/components/subtasks/');
  add('../components/comments/', '../features/tasks/components/comments/');
  add('../components/attachments/AttachmentList', '../features/tasks/components/attachments/AttachmentList');
  add('../components/TimeTrackingSection', '../features/tasks/components/TimeTrackingSection');

  add('../components/projects/', '../features/projects/components/projects/');
  add('../components/statuses/', '../features/projects/components/statuses/');
  add('../components/members/', '../features/projects/components/members/');
  add('../components/labels/', '../features/projects/components/labels/');
  add('../components/sprints/', '../features/sprints/components/sprints/');
  add('../components/notifications/', '../features/notifications/components/notifications/');
  add('../components/automation/', '../features/integrations/components/automation/');
  add('../components/dashboard/', '../features/analytics/components/dashboard/');

  // Auth components
  add('../components/ProtectedRoute', '../features/auth/components/ProtectedRoute');
  add('../components/PermissionRoute', '../features/auth/components/PermissionRoute');
  add('../components/WorkspaceOwnerRoute', '../features/auth/components/WorkspaceOwnerRoute');
  add('../components/TwoFactorModal', '../features/auth/components/TwoFactorModal');
  add('../components/ChangePasswordModal', '../features/auth/components/ChangePasswordModal');
  add('../components/OnboardingFlow', '../features/auth/components/OnboardingFlow');
  add('../components/RoleGuard', '../features/auth/components/RoleGuard');

  // Settings components
  add('../components/ThemeToggle', '../features/settings/components/ThemeToggle');
  add('../components/WorkspaceSwitcher', '../features/settings/components/WorkspaceSwitcher');
  add('../components/UserFormModal', '../features/settings/components/UserFormModal');
  add('../components/RoleFormModal', '../features/settings/components/RoleFormModal');
  add('../components/AssignRoleModal', '../features/settings/components/AssignRoleModal');

  // System components
  add('../components/UserTable', '../features/system/components/UserTable');
  add('../components/RoleTable', '../features/system/components/RoleTable');
  add('../components/SystemRoleModal', '../features/system/components/SystemRoleModal');

  // Analytics components
  add('../components/StatsSection', '../features/analytics/components/StatsSection');
  add('../components/StatCard', '../features/analytics/components/StatCard');
  add('../components/ActivityFeed', '../features/analytics/components/ActivityFeed');
  add('../components/ExportDropdown', '../features/analytics/components/ExportDropdown');

  // Project root components
  add('../components/ProjectCard', '../features/projects/components/ProjectCard');
  add('../components/ProjectFormModal', '../features/projects/components/ProjectFormModal');
  add('../components/ProjectNavBar', '../features/projects/components/ProjectNavBar');
  add('../components/TemplateSelector', '../features/projects/components/TemplateSelector');

  // Hooks
  add('../hooks/useNotificationWebSocket', '../features/notifications/hooks/useNotificationWebSocket');
  add('../hooks/useOptimisticMutation', '../features/tasks/hooks/useOptimisticMutation');

  return replacements;
}

function applyGlobalReplacements() {
  const replacements = buildGlobalReplacements();
  const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        if (entry.name === 'node_modules') continue;
        walk(full);
      } else if (/\.(jsx?|tsx?)$/.test(entry.name)) {
        let content = fs.readFileSync(full, 'utf8');
        let changed = false;
        for (const [regex, to] of replacements) {
          const next = content.replace(regex, to);
          if (next !== content) {
            content = next;
            changed = true;
          }
        }
        if (changed) {
          fs.writeFileSync(full, content);
          console.log(`UPDATED imports: ${path.relative(SRC, full)}`);
        }
      }
    }
  };
  walk(SRC);
}

function fixIntraFeatureApiImports() {
  // After global replace, files inside features/X may have wrong paths like
  // ../../../features/X/api/ - fix back to ../api/
  const featuresDir = path.join(SRC, 'features');
  const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) walk(full);
      else if (/\.(jsx?|tsx?)$/.test(entry.name)) {
        const parts = full.split(path.sep);
        const featIdx = parts.indexOf('features');
        if (featIdx === -1) continue;
        const feature = parts[featIdx + 1];
        let content = fs.readFileSync(full, 'utf8');
        const regex = new RegExp(`\\.\\./\\.\\./\\.\\./features/${feature}/`, 'g');
        const next = content.replace(regex, '../');
        if (next !== content) {
          fs.writeFileSync(full, next);
          console.log(`FIXED intra-feature: ${path.relative(SRC, full)}`);
        }
      }
    }
  };
  walk(featuresDir);
}

function fixCrossFeatureImportsInProjects() {
  const projectsDir = path.join(SRC, 'features', 'projects');
  if (!fs.existsSync(projectsDir)) return;

  const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) walk(full);
      else if (/\.(jsx?|tsx?)$/.test(entry.name)) {
        let content = fs.readFileSync(full, 'utf8');
        let changed = false;

        const rules = [
          [/from (["'])\.\.\/\.\.\/\.\.\/features\/tasks\//g, 'from $1../../tasks/'],
          [/from (["'])\.\.\/\.\.\/features\/tasks\//g, 'from $1../../tasks/'],
          [/from (["'])\.\.\/\.\.\/\.\.\/features\/sprints\//g, 'from $1../../sprints/'],
        ];

        for (const [regex, rep] of rules) {
          const next = content.replace(regex, rep);
          if (next !== content) {
            content = next;
            changed = true;
          }
        }

        if (changed) {
          fs.writeFileSync(full, content);
          console.log(`FIXED cross-feature (projects): ${path.relative(SRC, full)}`);
        }
      }
    }
  };
  walk(projectsDir);
}

function fixApiAxiosImports() {
  const featuresDir = path.join(SRC, 'features');
  const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) walk(full);
      else if (/features[\\/].+[\\/]api[\\/].+\.(jsx?|tsx?)$/.test(full)) {
        let content = fs.readFileSync(full, 'utf8');
        let changed = false;
        const rules = [
          [/from (["'])\.\.\/api\/axios/g, `from $1../../../api/axios`],
          [/from (["'])\.\.\/\.\.\/api\/axios/g, `from $1../../../api/axios`],
        ];
        for (const [regex, rep] of rules) {
          const next = content.replace(regex, rep);
          if (next !== content) {
            content = next;
            changed = true;
          }
        }
        if (changed) {
          fs.writeFileSync(full, content);
          console.log(`FIXED axios import: ${path.relative(SRC, full)}`);
        }
      }
    }
  };
  walk(featuresDir);
}

function createBarrelExports() {
  const barrels = {
    auth: {
      api: ['twoFactorApi', 'invitationApi'],
      reexportSharedApi: ['userApi'],
      components: [
        'ProtectedRoute', 'PermissionRoute', 'WorkspaceOwnerRoute',
        'TwoFactorModal', 'ChangePasswordModal', 'OnboardingFlow', 'RoleGuard',
      ],
      pages: [
        'Login', 'Register', 'ForgotPassword', 'ResetPassword', 'OAuthSuccess',
        'RegistrationSuccess', 'VerifyEmail', 'TwoFactorSetup', 'InvitationAccept',
        'WorkspaceInvitationAccept', 'Profile', 'Forbidden',
      ],
    },
    crm: {
      api: ['crmApi'],
      pages: [
        'CrmDashboard', 'LeadsPage', 'CustomersPage', 'ContactsPage', 'OpportunitiesPage',
        'PipelinePage', 'LeadDetailPage', 'CustomerDetailPage',
      ],
      components: [
        'ActivityFormModal', 'ContactFormModal', 'CustomerFormModal',
        'LeadFormModal', 'OpportunityFormModal',
      ],
    },
    finance: {
      api: ['financeApi'],
      pages: ['FinanceDashboard', 'InvoicesPage', 'InvoiceDetailPage', 'PaymentsPage', 'ExpensesPage'],
    },
    hr: {
      api: ['hrApi'],
      pages: [
        'EmployeesPage', 'DepartmentsPage', 'DesignationsPage', 'AttendancePage', 'ShiftsPage',
        'HolidaysPage', 'LeaveTypesPage', 'LeaveRequestsPage', 'LeaveApprovalsPage',
        'MyProfilePage', 'MyAttendancePage', 'MyLeavesPage', 'MyDocumentsPage',
        'HRAnalyticsPage', 'UtilizationReportPage', 'Timesheet', 'TimesheetApprovals', 'EmployeeDashboard',
      ],
      components: [
        'AttendanceFormModal', 'DepartmentFormModal', 'DesignationFormModal', 'EmployeeFormModal',
        'HolidayFormModal', 'LeaveRequestFormModal', 'LeaveTypeFormModal', 'ProjectAssignmentModal',
        'ProjectAssignmentPage', 'EmployeeProjectList', 'SelfServiceLeaveModal', 'ShiftFormModal',
      ],
    },
    projects: {
      api: ['projectApi', 'projectMembersApi', 'projectTemplateApi', 'boardApi', 'statusApi', 'labelApi'],
      pages: [
        'Projects', 'ProjectDetails', 'ProjectMembers', 'ProjectSettings', 'ProjectInvitations',
        'CreateProjectPage', 'Board', 'WorkflowStatuses',
      ],
      components: ['ProjectCard', 'ProjectFormModal', 'ProjectNavBar', 'TemplateSelector'],
    },
    tasks: {
      api: [
        'taskApi', 'subtaskApi', 'taskDependencyApi', 'commentApi', 'attachmentApi',
        'recurringApi', 'ganttApi', 'calendarApi',
      ],
      reexportSharedApi: ['timeTrackingApi'],
      pages: ['Tasks', 'TaskDetailPage', 'CreateTaskPage', 'GanttPage', 'CalendarPage'],
      hooks: ['useOptimisticMutation'],
      components: ['TimeTrackingSection'],
    },
    sprints: {
      api: ['sprintApi'],
      pages: ['Sprints'],
    },
    notifications: {
      api: ['notificationApi'],
      pages: ['Notifications'],
      hooks: ['useNotificationWebSocket'],
    },
    analytics: {
      api: ['analyticsApi', 'dashboardApi', 'exportApi', 'activityApi'],
      pages: ['Analytics', 'ExecutiveInsights', 'JobDashboard', 'UserHome'],
      components: ['StatsSection', 'StatCard', 'ActivityFeed', 'ExportDropdown'],
    },
    settings: {
      api: ['workspaceApi', 'notesApi', 'searchApi', 'permissionApi', 'roleApi'],
      reexportSharedApi: ['userApi'],
      pages: [
        'Settings', 'SystemSettings', 'WorkspaceSettings', 'Manage', 'Workspaces',
        'WorkspaceMembers', 'WorkspaceInvitations', 'WorkspaceDashboard', 'WorkspaceActivity',
        'WorkspaceNotes', 'MemberDirectory', 'KnowledgeHub',
      ],
      components: [
        'ThemeToggle', 'WorkspaceSwitcher', 'UserFormModal', 'RoleFormModal', 'AssignRoleModal',
      ],
    },
    system: {
      api: ['systemApi'],
      reexportSharedApi: ['userApi', 'roleApi', 'permissionApi'],
      pages: ['AuditLogs', 'SystemHealth', 'Users', 'Roles', 'SuperAdminDashboard'],
      components: ['UserTable', 'RoleTable', 'SystemRoleModal'],
    },
    integrations: {
      api: ['automationApi'],
      reexportSharedApi: ['calendarApi'],
      pages: ['Webhooks', 'SlackIntegration', 'AutomationRules'],
    },
  };

  for (const [feature, config] of Object.entries(barrels)) {
    const lines = [];
    const featDir = path.join(SRC, 'features', feature);

    for (const api of config.api || []) {
      const ext = fs.existsSync(path.join(featDir, 'api', `${api}.ts`)) ? 'ts' : 'js';
      if (fs.existsSync(path.join(featDir, 'api', `${api}.${ext}`))) {
        lines.push(`export * from './api/${api}';`);
      }
    }
    for (const api of config.reexportSharedApi || []) {
      lines.push(`export * from '../../api/${api}';`);
    }
    for (const hook of config.hooks || []) {
      if (fs.existsSync(path.join(featDir, 'hooks', `${hook}.js`))) {
        lines.push(`export { default as ${hook} } from './hooks/${hook}';`);
        lines.push(`export * from './hooks/${hook}';`);
      }
    }
    for (const comp of config.components || []) {
      if (fs.existsSync(path.join(featDir, 'components', `${comp}.jsx`))) {
        lines.push(`export { default as ${comp} } from './components/${comp}';`);
      }
    }
    for (const page of config.pages || []) {
      if (fs.existsSync(path.join(featDir, 'pages', `${page}.jsx`))) {
        lines.push(`export { default as ${page} } from './pages/${page}';`);
      }
    }

    const indexPath = path.join(featDir, 'index.ts');
    fs.writeFileSync(indexPath, `${lines.join('\n')}\n`);
    console.log(`WROTE barrel: features/${feature}/index.ts (${lines.length} exports)`);
  }
}

function removeEmptyDirs(dir) {
  if (!fs.existsSync(dir)) return;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (entry.isDirectory()) removeEmptyDirs(path.join(dir, entry.name));
  }
  if (fs.readdirSync(dir).length === 0 && dir !== SRC) {
    fs.rmdirSync(dir);
    console.log(`REMOVED empty dir: ${path.relative(SRC, dir)}`);
  }
}

// Run migration
console.log('=== Phase 1: Move files ===');
const moved = executeMoves();

console.log('\n=== Phase 2: Fix shared imports in feature files ===');
fixSharedImportsInFeatureFiles();

console.log('\n=== Phase 3: Global import path updates ===');
applyGlobalReplacements();

console.log('\n=== Phase 4: Fix intra-feature paths ===');
fixIntraFeatureApiImports();
fixCrossFeatureImportsInProjects();
fixApiAxiosImports();

console.log('\n=== Phase 5: Create barrel exports ===');
createBarrelExports();

console.log('\n=== Phase 6: Remove empty legacy dirs ===');
removeEmptyDirs(path.join(SRC, 'components', 'tasks'));
removeEmptyDirs(path.join(SRC, 'components', 'projects'));
removeEmptyDirs(path.join(SRC, 'components', 'statuses'));
removeEmptyDirs(path.join(SRC, 'components', 'members'));
removeEmptyDirs(path.join(SRC, 'components', 'labels'));
removeEmptyDirs(path.join(SRC, 'components', 'sprints'));
removeEmptyDirs(path.join(SRC, 'components', 'notifications'));
removeEmptyDirs(path.join(SRC, 'components', 'automation'));
removeEmptyDirs(path.join(SRC, 'components', 'dashboard'));
removeEmptyDirs(path.join(SRC, 'components', 'subtasks'));
removeEmptyDirs(path.join(SRC, 'components', 'comments'));
removeEmptyDirs(path.join(SRC, 'components', 'attachments'));

console.log(`\nDone. Moved ${moved.length} file entries.`);
