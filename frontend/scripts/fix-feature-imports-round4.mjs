import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const SRC = path.join(__dirname, '..', 'src');
const FEATURES = path.join(SRC, 'features');

function listFeatureComponents(feature) {
  const compDir = path.join(FEATURES, feature, 'components');
  const names = new Set();
  if (!fs.existsSync(compDir)) return names;

  const walk = (dir, prefix = '') => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const rel = prefix ? `${prefix}/${entry.name}` : entry.name;
      if (entry.isDirectory()) walk(path.join(dir, entry.name), rel);
      else if (/\.(jsx|tsx|js|ts)$/.test(entry.name)) {
        names.add(rel.replace(/\.(jsx|tsx|js|ts)$/, ''));
        names.add(path.basename(entry.name).replace(/\.(jsx|tsx|js|ts)$/, ''));
      }
    }
  };
  walk(compDir);
  return names;
}

function walk(dir, cb) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(full, cb);
    else if (/\.(jsx?|tsx?)$/.test(entry.name)) cb(full);
  }
}

for (const feature of fs.readdirSync(FEATURES)) {
  const featDir = path.join(FEATURES, feature);
  if (!fs.statSync(featDir).isDirectory()) continue;

  const localComponents = listFeatureComponents(feature);
  if (localComponents.size === 0) continue;

  walk(featDir, (filePath) => {
    if (filePath.includes(`${path.sep}api${path.sep}`)) return;

    let content = fs.readFileSync(filePath, 'utf8');
    let changed = false;

    const rel = path.relative(featDir, filePath).replace(/\\/g, '/');
    const dirDepth = rel.split('/').length - 1;
    const intraPrefix = '../'.repeat(dirDepth) + 'components/';

    for (const comp of localComponents) {
      const compBase = comp.includes('/') ? comp.split('/').pop() : comp;
      // Fix wrong shared-style paths pointing at local components
      for (const wrongDepth of [3, 4, 5]) {
        const wrong = `${'../'.repeat(wrongDepth)}components/${compBase}`;
        const right = `${intraPrefix}${compBase}`;
        if (wrong !== right && content.includes(wrong)) {
          content = content.split(wrong).join(right);
          changed = true;
        }
        // full path like components/sub/Name
        if (comp.includes('/')) {
          const wrongFull = `${'../'.repeat(wrongDepth)}components/${comp}`;
          const rightFull = `${intraPrefix}${comp}`;
          if (wrongFull !== rightFull && content.includes(wrongFull)) {
            content = content.split(wrongFull).join(rightFull);
            changed = true;
          }
        }
      }
    }

    if (changed) {
      fs.writeFileSync(filePath, content);
      console.log(`FIXED local components: ${path.relative(SRC, filePath)}`);
    }
  });
}

console.log('Round 4 complete.');
