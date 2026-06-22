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
  const dirDepth = rest.split('/').length - 1;
  return { feature, dirDepth, rel };
}

function crossPrefix(dirDepth) {
  return '../'.repeat(dirDepth + 1);
}

walk(path.join(SRC, 'features'), (filePath) => {
  let content = fs.readFileSync(filePath, 'utf8');
  const info = getFeatureInfo(filePath);
  if (!info) return;

  const { feature, dirDepth } = info;
  let changed = false;

  // Fix cross-feature imports containing /features/
  content = content.replace(
    /from (["'])(?:\.\.\/)+features\/([^"']+)\1/g,
    (match, quote, targetPath) => {
      changed = true;
      return `from ${quote}${crossPrefix(dirDepth)}${targetPath}${quote}`;
    },
  );

  // Fix same-feature imports containing /features/FEATURE/
  content = content.replace(
    new RegExp(`from (["'])(?:\\.\\./)+features/${feature}/([^"']+)\\1`, 'g'),
    (match, quote, rest) => {
      changed = true;
      return `from ${quote}${'../'.repeat(dirDepth)}${rest}${quote}`;
    },
  );

  // Fix over-deep intra-feature API imports in nested component dirs
  if (dirDepth >= 2) {
    const correct = `${'../'.repeat(dirDepth)}api/`;
    for (const api of FEATURE_APIS[feature] || []) {
      for (const depth of ['../../../../', '../../../']) {
        const wrong = `${depth}api/${api}`;
        const right = `${correct}${api}`;
        if (content.includes(wrong)) {
          content = content.split(wrong).join(right);
          changed = true;
        }
        const wrongJs = `${wrong}.js`;
        const rightJs = `${correct}${api}.js`;
        if (content.includes(wrongJs)) {
          content = content.split(wrongJs).join(rightJs);
          changed = true;
        }
      }
    }
  }

  if (changed) {
    fs.writeFileSync(filePath, content);
    console.log(`FIXED: ${path.relative(SRC, filePath)}`);
  }
});

console.log('Round 3 complete.');
