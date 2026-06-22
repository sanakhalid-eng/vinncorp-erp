import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const SRC = path.join(__dirname, '..', 'src');

const FEATURE_APIS = {
  auth: ['twoFactorApi', 'invitationApi'],
  finance: ['financeApi'],
  hr: ['hrApi'],
  projects: ['projectApi', 'projectMembersApi', 'projectTemplateApi', 'boardApi', 'statusApi', 'labelApi'],
  tasks: ['taskApi', 'subtaskApi', 'taskDependencyApi', 'commentApi', 'attachmentApi', 'recurringApi', 'ganttApi', 'calendarApi'],
  sprints: ['sprintApi'],
  notifications: ['notificationApi'],
  analytics: ['analyticsApi', 'dashboardApi', 'exportApi', 'activityApi'],
  settings: ['workspaceApi', 'notesApi', 'searchApi', 'permissionApi', 'roleApi'],
  system: ['systemApi'],
  integrations: ['automationApi'],
  crm: ['crmApi'],
};

function depthToFeatureRoot(filePath) {
  const rel = path.relative(SRC, filePath).replace(/\\/g, '/');
  const match = rel.match(/^features\/([^/]+)\/(.+)$/);
  if (!match) return null;
  const [, feature, rest] = match;
  const segments = rest.split('/');
  // pages/file or components/file = depth 2 from feature root
  // components/sub/file = depth 3 from feature root
  const depthFromFeature = segments.length;
  return { feature, depthFromFeature, segments };
}

function upLevels(n) {
  return '../'.repeat(n);
}

function fixFile(filePath) {
  const info = depthToFeatureRoot(filePath);
  if (!info) return false;

  const { feature, depthFromFeature } = info;
  const featureApis = FEATURE_APIS[feature] || [];
  let content = fs.readFileSync(filePath, 'utf8');
  let changed = false;

  const apply = (regex, replacement) => {
    const next = content.replace(regex, replacement);
    if (next !== content) {
      content = next;
      changed = true;
    }
  };

  // API files: fix axios import
  if (filePath.includes(`${path.sep}api${path.sep}`) && /\.(ts|js)$/.test(filePath)) {
    apply(/from (["'])\.\/axios(\.js)?\1/g, `from $1../../../api/axios$2$1`);
    apply(/from (["'])\.\.\/api\/axios/g, `from $1../../../api/axios`);
  }

  // Compute prefix to reach feature root from current file
  const toFeature = upLevels(depthFromFeature);
  const toSrc = upLevels(depthFromFeature + 1);

  for (const api of featureApis) {
    // Fix over-corrected shared paths back to intra-feature api
    apply(
      new RegExp(`from (["'])\\.\\./\\.\\./\\.\\./api/${api}(\\.js)?\\1`, 'g'),
      `from $1${toFeature}api/${api}$2$1`,
    );
    apply(
      new RegExp(`from (["'])\\.\\./\\.\\./\\.\\./\\.\\./api/${api}(\\.js)?\\1`, 'g'),
      `from $1${toFeature}api/${api}$2$1`,
    );

    // Fix wrong single-level api imports in nested component dirs
    if (depthFromFeature >= 3) {
      apply(
        new RegExp(`from (["'])\\.\\./api/${api}(\\.js)?\\1`, 'g'),
        `from $1${toFeature}api/${api}$2$1`,
      );
    }
  }

  // Fix shared src imports depth for nested component dirs (depth >= 3)
  if (depthFromFeature >= 3) {
    const sharedPrefixes = ['api', 'context', 'components', 'hooks', 'utils', 'lib', 'constants', 'assets', 'layouts', 'types'];
    for (const prefix of sharedPrefixes) {
      // Wrong 3-level prefix when 4 needed
      apply(
        new RegExp(`from (["'])\\.\\./\\.\\./\\.\\./${prefix}/`, 'g'),
        `from $1${toSrc}${prefix}/`,
      );
    }
  }

  if (changed) {
    fs.writeFileSync(filePath, content);
    console.log(`FIXED: ${path.relative(SRC, filePath)}`);
  }
  return changed;
}

// Fix App.jsx auth component imports
const appPath = path.join(SRC, 'App.jsx');
let app = fs.readFileSync(appPath, 'utf8');
const appFixed = app
  .replace('./components/ProtectedRoute', './features/auth/components/ProtectedRoute')
  .replace('./components/WorkspaceOwnerRoute', './features/auth/components/WorkspaceOwnerRoute');
if (appFixed !== app) {
  fs.writeFileSync(appPath, appFixed);
  console.log('FIXED: App.jsx');
}

// Fix remaining legacy imports across src
const legacyReplacements = [
  ['./components/ProtectedRoute', './features/auth/components/ProtectedRoute'],
  ['./components/PermissionRoute', './features/auth/components/PermissionRoute'],
  ['./components/WorkspaceOwnerRoute', './features/auth/components/WorkspaceOwnerRoute'],
  ['../components/ProtectedRoute', '../features/auth/components/ProtectedRoute'],
  ['../components/PermissionRoute', '../features/auth/components/PermissionRoute'],
  ['../components/WorkspaceOwnerRoute', '../features/auth/components/WorkspaceOwnerRoute'],
  ['../api/permissionApi', '../features/settings/api/permissionApi'],
  ['../api/roleApi', '../features/settings/api/roleApi'],
  ['../api/workspaceApi', '../features/settings/api/workspaceApi'],
  ['../api/projectApi', '../features/projects/api/projectApi'],
  ['../api/projectMembersApi', '../features/projects/api/projectMembersApi'],
  ['../api/taskApi', '../features/tasks/api/taskApi'],
  ['../api/sprintApi', '../features/sprints/api/sprintApi'],
  ['../api/notificationApi', '../features/notifications/api/notificationApi'],
  ['../api/systemApi', '../features/system/api/systemApi'],
  ['../api/automationApi', '../features/integrations/api/automationApi'],
  ['../api/invitationApi', '../features/auth/api/invitationApi'],
  ['../api/twoFactorApi', '../features/auth/api/twoFactorApi'],
  ['../api/financeApi', '../features/finance/api/financeApi'],
  ['../api/analyticsApi', '../features/analytics/api/analyticsApi'],
  ['../api/dashboardApi', '../features/analytics/api/dashboardApi'],
  ['../api/activityApi', '../features/analytics/api/activityApi'],
  ['../api/exportApi', '../features/analytics/api/exportApi'],
  ['../api/ganttApi', '../features/tasks/api/ganttApi'],
  ['../api/calendarApi', '../features/tasks/api/calendarApi'],
  ['../api/notesApi', '../features/settings/api/notesApi'],
  ['../api/searchApi', '../features/settings/api/searchApi'],
  ['../hooks/useNotificationWebSocket', '../features/notifications/hooks/useNotificationWebSocket'],
];

function walk(dir, cb) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (entry.name === 'node_modules') continue;
      walk(full, cb);
    } else if (/\.(jsx?|tsx?)$/.test(entry.name)) {
      cb(full);
    }
  }
}

walk(SRC, fixFile);

walk(SRC, (filePath) => {
  if (!filePath.includes(`${path.sep}src${path.sep}`) && !filePath.startsWith(SRC)) return;
  let content = fs.readFileSync(filePath, 'utf8');
  let changed = false;
  for (const [from, to] of legacyReplacements) {
    if (content.includes(from)) {
      // Skip if already in features and importing same-feature api via ../api
      const rel = path.relative(SRC, filePath);
      if (rel.startsWith('features') && from.startsWith('../api/')) {
        const apiName = from.replace('../api/', '');
        const feat = rel.split(/[/\\]/)[1];
        if (FEATURE_APIS[feat]?.some((a) => apiName.startsWith(a))) continue;
      }
      const next = content.split(from).join(to);
      if (next !== content) {
        content = next;
        changed = true;
      }
    }
  }
  if (changed) {
    fs.writeFileSync(filePath, content);
    console.log(`LEGACY FIX: ${path.relative(SRC, filePath)}`);
  }
});

console.log('Import fix complete.');
