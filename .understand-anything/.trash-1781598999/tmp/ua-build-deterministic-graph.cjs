const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const root = process.argv[2];
const skillDir = process.argv[3];
if (!root || !skillDir) {
  console.error('Usage: node ua-build-deterministic-graph.cjs <projectRoot> <skillDir>');
  process.exit(1);
}

const ua = path.join(root, '.understand-anything');
const tmp = path.join(ua, 'tmp');
const inter = path.join(ua, 'intermediate');
const batchesPath = path.join(inter, 'batches.json');
const scanPath = path.join(inter, 'scan-result.json');
const extractScript = path.join(skillDir, 'extract-structure.mjs');
const utf8 = new TextEncoder();

function readJson(file) {
  let text = fs.readFileSync(file, 'utf8');
  if (text.charCodeAt(0) === 0xfeff) text = text.slice(1);
  return JSON.parse(text);
}

function writeJson(file, value) {
  fs.writeFileSync(file, Buffer.from(utf8.encode(JSON.stringify(value, null, 2))));
}

function slash(p) {
  return p.replace(/\\/g, '/');
}

function baseName(p) {
  return path.posix.basename(slash(p));
}

function prefixForFile(file) {
  const p = slash(file.path);
  if (file.fileCategory === 'config') return 'config';
  if (file.fileCategory === 'docs') return 'document';
  if (file.fileCategory === 'infra') {
    if (/^\.github\/workflows\//.test(p) || /(^|\/)(Jenkinsfile|\.gitlab-ci\.yml)$/.test(p)) return 'pipeline';
    if (/dockerfile/i.test(baseName(p)) || /docker-compose|compose\.ya?ml/i.test(baseName(p))) return 'service';
    return 'config';
  }
  if (file.fileCategory === 'data') {
    if (/\.(sql|graphql|gql|proto|prisma)$/i.test(p)) return 'schema';
    return 'file';
  }
  return 'file';
}

function fileId(file) {
  return `${prefixForFile(file)}:${slash(file.path)}`;
}

function complexity(lines) {
  if (lines >= 180) return 'complex';
  if (lines >= 60) return 'moderate';
  return 'simple';
}

function tagsFor(file, result) {
  const p = slash(file.path).toLowerCase();
  const tags = new Set([file.language || 'unknown', file.fileCategory || 'code']);
  for (const token of ['controller', 'service', 'repository', 'mapper', 'dto', 'entity', 'config', 'security', 'test', 'migration']) {
    if (p.includes(token)) tags.add(token);
  }
  if (result?.metrics?.classCount) tags.add('class');
  if (result?.metrics?.functionCount) tags.add('function');
  return Array.from(tags).slice(0, 5);
}

function summaryFor(file, result) {
  const name = baseName(file.path);
  const parts = [];
  if (result?.metrics?.classCount) parts.push(`${result.metrics.classCount} class`);
  if (result?.metrics?.functionCount) parts.push(`${result.metrics.functionCount} function`);
  if (result?.metrics?.importCount) parts.push(`${result.metrics.importCount} import`);
  const detail = parts.length ? `, gồm ${parts.join(', ')}` : '';
  const category = file.fileCategory === 'docs' ? 'tài liệu' :
    file.fileCategory === 'config' ? 'cấu hình' :
    file.fileCategory === 'infra' ? 'hạ tầng' :
    file.fileCategory === 'data' ? 'dữ liệu/schema' :
    file.fileCategory === 'script' ? 'script' : 'mã nguồn';
  return `${name} là file ${category} ${file.language || 'unknown'} trong VocabVerse${detail}.`;
}

function significantFunction(fn, exports) {
  const len = (fn.endLine || fn.startLine || 0) - (fn.startLine || 0) + 1;
  return len >= 10 || exports.has(fn.name);
}

function significantClass(cls, exports) {
  const len = (cls.endLine || cls.startLine || 0) - (cls.startLine || 0) + 1;
  return (cls.methods || []).length >= 2 || len >= 20 || exports.has(cls.name);
}

function edge(source, target, type, weight) {
  return { source, target, type, direction: 'forward', weight };
}

const scan = readJson(scanPath);
const batches = readJson(batchesPath).batches || [];
const fileByPath = new Map(scan.files.map(f => [slash(f.path), f]));

for (const batch of batches) {
  const batchIndex = batch.batchIndex;
  const input = {
    projectRoot: root,
    batchFiles: batch.files,
    batchImportData: batch.batchImportData || {},
  };
  const inputPath = path.join(tmp, `ua-file-analyzer-input-${batchIndex}.json`);
  const extractPath = path.join(tmp, `ua-file-extract-results-${batchIndex}.json`);
  writeJson(inputPath, input);

  const run = spawnSync(process.execPath, [extractScript, inputPath, extractPath], {
    cwd: root,
    encoding: 'utf8',
    env: process.env,
  });
  if (run.stderr) process.stderr.write(run.stderr);
  if (run.status !== 0) {
    console.error(`extract-structure failed for batch ${batchIndex}`);
    process.exit(run.status || 1);
  }
  const extracted = readJson(extractPath);
  const resultByPath = new Map((extracted.results || []).map(r => [slash(r.path), r]));
  const nodes = [];
  const edges = [];
  const nodeIds = new Set();

  for (const file of batch.files) {
    const p = slash(file.path);
    const result = resultByPath.get(p);
    const id = fileId(file);
    const exportNames = new Set((result?.exports || []).map(e => e.name));
    const fileNode = {
      id,
      type: prefixForFile(file),
      name: baseName(p),
      filePath: p,
      summary: summaryFor(file, result),
      tags: tagsFor(file, result),
      complexity: complexity(file.sizeLines || result?.totalLines || 0),
    };
    nodes.push(fileNode);
    nodeIds.add(id);

    for (const cls of result?.classes || []) {
      if (!significantClass(cls, exportNames)) continue;
      const cid = `class:${p}:${cls.name}`;
      nodes.push({
        id: cid,
        type: 'class',
        name: cls.name,
        filePath: p,
        lineRange: [cls.startLine || 1, cls.endLine || cls.startLine || 1],
        summary: `${cls.name} định nghĩa cấu trúc hoặc hành vi chính trong ${baseName(p)}.`,
        tags: ['java', 'class', p.includes('/controller/') ? 'controller' : p.includes('/service/') ? 'service' : 'domain'],
        complexity: complexity((cls.endLine || 0) - (cls.startLine || 0) + 1),
      });
      nodeIds.add(cid);
      edges.push(edge(id, cid, 'contains', 1.0));
      if (exportNames.has(cls.name)) edges.push(edge(id, cid, 'exports', 0.8));
    }

    for (const fn of result?.functions || []) {
      if (!significantFunction(fn, exportNames)) continue;
      const fid = `function:${p}:${fn.name}`;
      nodes.push({
        id: fid,
        type: 'function',
        name: fn.name,
        filePath: p,
        lineRange: [fn.startLine || 1, fn.endLine || fn.startLine || 1],
        summary: `${fn.name} triển khai một thao tác đáng chú ý trong ${baseName(p)}.`,
        tags: ['function', file.language || 'code', p.includes('/test/') ? 'test' : 'application'],
        complexity: complexity((fn.endLine || 0) - (fn.startLine || 0) + 1),
      });
      nodeIds.add(fid);
      edges.push(edge(id, fid, 'contains', 1.0));
      if (exportNames.has(fn.name)) edges.push(edge(id, fid, 'exports', 0.8));
    }

    for (const targetPath of batch.batchImportData?.[p] || []) {
      const targetFile = fileByPath.get(slash(targetPath));
      if (!targetFile) continue;
      const target = fileId(targetFile);
      if (target !== id) edges.push(edge(id, target, 'imports', 0.7));
    }
  }

  writeJson(path.join(inter, `batch-${batchIndex}.json`), { nodes, edges });
  console.log(`batch ${batchIndex}: ${nodes.length} nodes, ${edges.length} edges`);
}
