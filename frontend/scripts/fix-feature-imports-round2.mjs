import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const SRC = path.join(__dirname, '..', 'src');

const FEATURE_APIS = {
  auth: ['twoFactorApi', 'invitationApi'],
  finance: ['financeApi'],
  hr: ['hrApi'],
  crm: ['crmApi'],
  projects: ['projectApi', 'projectMembersApi', 'projectTemplateApi', 'boardApi', 'statusApi', 'labelApi'],
  tasks: ['taskApi', 'subtaskApi', 'taskDependencyApi', 'commentApi', 'attachmentApi', 'recurringApi', 'ganttApi', 'calendarApi'],
  sprints: ['sprintApi'],
  notifications: ['notificationApi'],
  analytics: ['analyticsApi', 'dashboardApi', 'exportApi', 'activityApi'],
  settings: ['workspaceApi', 'notesApi', 'searchApi', 'permissionApi', 'roleApi'],
  system: ['systemApi'],
  integrations: ['automationApi'],
};

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

function getFeatureInfo(filePath) {
  const rel = path.relative(SRC, filePath).replace(/\\/g, '/');
  const m = rel.match(/^features\/([^/]+)\/(.+)$/);
  if (!m) return null;
  const [, feature, rest] = m;
  const dirDepth = rest.split('/').length - 1; // dirs under feature before filename
  return { feature, dirDepth, rel };
}

function up(n) {
  return '../'.repeat(n);
}

// Manual targeted fixes
const manualFixes = [
  ['src/components/Navbar.jsx', [
    ['./ThemeToggle', '../features/settings/components/ThemeToggle'],
  ]],
];

for (const [rel, pairs] of manualFixes) {
  const fp = path.join(SRC, rel.replace('src/', ''));
  let c = fs.readFileSync(fp, 'utf8');
  for (const [from, to] of pairs) {
    c = c.split(from).join(to);
  }
  fs.writeFileSync(fp, c);
  console.log(`MANUAL: ${rel}`);
}

walk(SRC, (filePath) => {
  let content = fs.readFileSync(filePath, 'utf8');
  let changed = false;
  const apply = (from, to) => {
    if (content.includes(from)) {
      content = content.split(from).join(to);
      changed = true;
    }
  };

  const info = getFeatureInfo(filePath);
  if (info) {
    const { feature, dirDepth } = info;
    const apis = FEATURE_APIS[feature] || [];

    // Fix wrong ../features/X/ paths inside features folder
    content = content.replace(/from (["'])\.\.\/features\/([^"']+)\1/g, (match, q, p) => {
      changed = true;
      const parts = p.split('/');
      const targetFeature = parts[0];
      const rest = parts.slice(1).join('/');
      if (targetFeature === feature) {
        // same feature
        const levels = dirDepth;
        return `from ${q}${up(levels)}${rest}${q}`;
      }
      // cross feature: from features/X/pages/file depth 1 -> ../../targetFeature/rest
      const levels = dirDepth + 1;
      return `from ${q}${up(levels)}${targetFeature}/${rest}${q}`;
    });

    // Fix pages using ../../api/ instead of ../api/ for same-feature APIs
    if (dirDepth === 1) {
      for (const api of apis) {
        apply(`from "../../api/${api}`, `from "../api/${api}`);
        apply(`from '../../api/${api}`, `from '../api/${api}`);
        apply(`from "../../api/${api}.js"`, `from "../api/${api}.js"`);
        apply(`from '../../api/${api}.js'`, `from '../api/${api}.js'`);
      }
    }

    // Fix nested components using wrong api depth (should be dirDepth levels up)
    if (dirDepth >= 2) {
      const correct = `${up(dirDepth)}api/`;
      for (const api of apis) {
        apply(`from "../api/${api}"`, `from "${correct}${api}"`);
        apply(`from '../api/${api}'`, `from '${correct}${api}'`);
        apply(`from "../api/${api}.js"`, `from "${correct}${api}.js"`);
        apply(`from '../api/${api}.js'`, `from '${correct}${api}.js'`);
      }
    }

    // Cross-feature dashboard components
    apply('../../../components/dashboard', '../../analytics/components/dashboard');
    apply('"../../../components/dashboard"', '"../../analytics/components/dashboard"');
  }

  // Global: moved component paths still pointing at old locations
  const globalPairs = [
    ['../components/dashboard/', '../features/analytics/components/dashboard/'],
    ['./components/dashboard/', './features/analytics/components/dashboard/'],
    ['../components/ExportDropdown', '../features/analytics/components/ExportDropdown'],
    ['../components/ActivityFeed', '../features/analytics/components/ActivityFeed'],
    ['../components/StatsSection', '../features/analytics/components/StatsSection'],
    ['../components/StatCard', '../features/analytics/components/StatCard'],
    ['../components/PermissionMatrix', '../components/PermissionMatrix'], // stays shared - no change
  ];

  for (const [from, to] of globalPairs) {
    if (from !== to) apply(from, to);
  }

  if (changed) {
    fs.writeFileSync(filePath, content);
    console.log(`FIXED: ${path.relative(SRC, filePath)}`);
  }
});

console.log('Round 2 complete.');
