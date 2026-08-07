const fs = require('fs');
const path = require('path');

const root = process.argv[2];
const commit = process.argv[3];
if (!root || !commit) {
  console.error('Usage: node ua-assemble-final.cjs <projectRoot> <gitCommitHash>');
  process.exit(1);
}

const ua = path.join(root, '.understand-anything');
const inter = path.join(ua, 'intermediate');
const scan = JSON.parse(stripBom(fs.readFileSync(path.join(inter, 'scan-result.json'), 'utf8')));
const assembled = JSON.parse(stripBom(fs.readFileSync(path.join(inter, 'assembled-graph.json'), 'utf8')));

function stripBom(text) {
  return text.charCodeAt(0) === 0xfeff ? text.slice(1) : text;
}

function writeJson(file, value) {
  fs.writeFileSync(file, JSON.stringify(value, null, 2));
}

function kebab(text) {
  return text.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
}

const fileLevelTypes = new Set(['file', 'config', 'document', 'service', 'pipeline', 'table', 'schema', 'resource', 'endpoint']);
const fileNodes = assembled.nodes.filter(n => fileLevelTypes.has(n.type));
const nodeIds = new Set(assembled.nodes.map(n => n.id));

const layerDefs = [
  ['layer:entrypoint-runtime', 'Entry Point & Runtime', 'Điểm khởi động Spring Boot và tài nguyên runtime chính của ứng dụng.'],
  ['layer:api-controllers', 'API Controllers', 'Các REST controller expose module Auth, Collection, Vocabulary, Learning, Admin, Notification và các API khác.'],
  ['layer:application-services', 'Application Services', 'Service, client, provider, parser, prompt và scheduler chứa workflow nghiệp vụ.'],
  ['layer:domain-data-access', 'Domain & Data Access', 'Entity, DTO, mapper, repository và model dữ liệu của modular monolith.'],
  ['layer:security-configuration', 'Security & Configuration', 'Cấu hình Spring, security, JWT, OpenAPI, Redis, RabbitMQ và application properties.'],
  ['layer:persistence-migrations', 'Persistence & Migrations', 'Flyway SQL migrations và schema dữ liệu nền cho PostgreSQL.'],
  ['layer:tests-quality', 'Tests & Quality', 'Unit/integration tests và dữ liệu kiểm thử.'],
  ['layer:build-deployment', 'Build & Deployment', 'Maven/Gradle wrapper, Docker, Compose và các file build/deploy.'],
  ['layer:documentation', 'Documentation', 'README, docs, Postman collection và ghi chú thiết kế/học thuật của dự án.'],
  ['layer:workspace-artifacts', 'Workspace Artifacts', 'Các artifact cục bộ được git track hoặc cache build không thuộc runtime chính.'],
];
const layers = layerDefs.map(([id, name, description]) => ({ id, name, description, nodeIds: [] }));
const byId = new Map(layers.map(l => [l.id, l]));

function chooseLayer(node) {
  const p = (node.filePath || '').replace(/\\/g, '/').toLowerCase();
  const name = (node.name || '').toLowerCase();
  if (p.includes('/src/test/') || /test/.test(name)) return 'layer:tests-quality';
  if (p.endsWith('/vocabverseapplication.java')) return 'layer:entrypoint-runtime';
  if (p.includes('/controller/')) return 'layer:api-controllers';
  if (p.includes('/service/') || p.includes('/client/') || p.includes('/provider/') || p.includes('/parser/') || p.includes('/prompt/') || p.includes('/strategy/')) return 'layer:application-services';
  if (p.includes('/entity/') || p.includes('/repository/') || p.includes('/dto/') || p.includes('/mapper/') || p.includes('/model/')) return 'layer:domain-data-access';
  if (p.includes('/security/') || p.includes('/config/') || p.includes('/filter/') || p.includes('/jwt/') || p.endsWith('application.yml') || p.endsWith('application.properties') || p.endsWith('.env') || p.endsWith('.env.example')) return 'layer:security-configuration';
  if (p.includes('/db/migration/') || p.endsWith('.sql')) return 'layer:persistence-migrations';
  if (node.type === 'document' || p.endsWith('.md') || p.endsWith('.postman_collection.json') || p.startsWith('docs/')) return 'layer:documentation';
  if (node.type === 'service' || node.type === 'pipeline' || p.includes('docker') || p.includes('gradle') || p.includes('mvn') || p.endsWith('pom.xml') || p.endsWith('build.gradle') || p.endsWith('settings.gradle')) return 'layer:build-deployment';
  if (p.includes('/src/main/resources/')) return 'layer:entrypoint-runtime';
  if (p.startsWith('.gradle/') || p.startsWith('build/') || p.startsWith('target/')) return 'layer:workspace-artifacts';
  return 'layer:domain-data-access';
}

for (const node of fileNodes) {
  byId.get(chooseLayer(node)).nodeIds.push(node.id);
}
for (const layer of layers) layer.nodeIds = Array.from(new Set(layer.nodeIds)).filter(id => nodeIds.has(id)).sort();

function existing(ids) {
  return ids.filter(id => nodeIds.has(id));
}

const tour = [
  {
    order: 1,
    title: 'Tổng quan VocabVerse',
    description: 'Bắt đầu từ README để hiểu backend Spring Boot cho nền tảng học từ vựng tiếng Anh dùng AI và các module chính.',
    nodeIds: existing(['document:README.md']),
  },
  {
    order: 2,
    title: 'Khởi động ứng dụng',
    description: 'Điểm vào Spring Boot bootstrap runtime và nạp cấu hình ứng dụng.',
    nodeIds: existing(['file:src/main/java/com/vocabverse/VocabVerseApplication.java', 'config:src/main/resources/application.yml']),
  },
  {
    order: 3,
    title: 'Bề mặt API',
    description: 'Các controller định nghĩa REST endpoints cho auth, vocabulary, collection, learning, admin và notification.',
    nodeIds: existing([
      'file:src/main/java/com/vocabverse/auth/controller/AuthController.java',
      'file:src/main/java/com/vocabverse/vocabulary/controller/VocabularyController.java',
      'file:src/main/java/com/vocabverse/collection/controller/CollectionController.java',
      'file:src/main/java/com/vocabverse/admin/controller/AdminDashboardController.java',
    ]),
  },
  {
    order: 4,
    title: 'Workflow nghiệp vụ',
    description: 'Service layer điều phối logic nghiệp vụ, gọi repository, AI client, scheduler và các provider phụ trợ.',
    nodeIds: existing([
      'file:src/main/java/com/vocabverse/auth/service/AuthService.java',
      'file:src/main/java/com/vocabverse/vocabulary/service/VocabularyService.java',
      'file:src/main/java/com/vocabverse/ai/service/AiVocabularyService.java',
      'file:src/main/java/com/vocabverse/review/service/ReviewService.java',
    ]),
  },
  {
    order: 5,
    title: 'Dữ liệu và persistence',
    description: 'Entity, repository và Flyway migrations mô tả dữ liệu PostgreSQL và các quan hệ chính.',
    nodeIds: existing([
      'file:src/main/java/com/vocabverse/user/entity/UserEntity.java',
      'file:src/main/java/com/vocabverse/vocabulary/entity/VocabularyEntity.java',
      'file:src/main/java/com/vocabverse/user/repository/UserRepository.java',
      'schema:src/main/resources/db/migration/V1__init.sql',
    ]),
  },
  {
    order: 6,
    title: 'Security và integration',
    description: 'Cấu hình security, JWT, OpenAPI, Redis/RabbitMQ và client tích hợp định hình runtime behavior.',
    nodeIds: existing([
      'file:src/main/java/com/vocabverse/common/config/SecurityConfig.java',
      'file:src/main/java/com/vocabverse/common/security/JwtAuthenticationFilter.java',
      'file:src/main/java/com/vocabverse/common/config/OpenApiConfig.java',
      'file:src/main/java/com/vocabverse/ai/client/GroqVocabularyClient.java',
    ]),
  },
  {
    order: 7,
    title: 'Kiểm thử và triển khai',
    description: 'Test suite, Maven/Gradle build files và Docker assets hỗ trợ quality gate và deployment local.',
    nodeIds: existing([
      'file:src/test/java/com/vocabverse/auth/service/AuthServiceTest.java',
      'config:pom.xml',
      'service:Dockerfile',
      'service:docker-compose.yml',
    ]),
  },
].filter(step => step.nodeIds.length > 0);

const graph = {
  version: '1.0.0',
  project: {
    name: scan.name,
    languages: scan.languages,
    frameworks: scan.frameworks,
    description: scan.description,
    analyzedAt: new Date().toISOString(),
    gitCommitHash: commit,
  },
  nodes: assembled.nodes,
  edges: assembled.edges,
  layers,
  tour,
};

const issues = [];
const warnings = [];
const assigned = new Map();
for (const layer of graph.layers) {
  for (const id of layer.nodeIds) {
    if (!nodeIds.has(id)) issues.push(`Layer '${layer.id}' refs missing node '${id}'`);
    if (assigned.has(id)) issues.push(`Node '${id}' appears in multiple layers`);
    assigned.set(id, layer.id);
  }
}
for (const node of fileNodes) {
  if (!assigned.has(node.id)) issues.push(`File node '${node.id}' not in any layer`);
}
for (const edge of graph.edges) {
  if (!nodeIds.has(edge.source)) issues.push(`Edge source '${edge.source}' not found`);
  if (!nodeIds.has(edge.target)) issues.push(`Edge target '${edge.target}' not found`);
}
const withEdges = new Set(graph.edges.flatMap(e => [e.source, e.target]));
for (const node of graph.nodes) {
  if (!withEdges.has(node.id)) warnings.push(`Node '${node.id}' has no edges`);
}
const stats = {
  totalNodes: graph.nodes.length,
  totalEdges: graph.edges.length,
  totalLayers: graph.layers.length,
  tourSteps: graph.tour.length,
  nodeTypes: countBy(graph.nodes, 'type'),
  edgeTypes: countBy(graph.edges, 'type'),
};

writeJson(path.join(inter, 'assembled-graph.json'), graph);
writeJson(path.join(inter, 'review.json'), { issues, warnings, stats });
console.log(`assembled final graph: ${stats.totalNodes} nodes, ${stats.totalEdges} edges, ${stats.totalLayers} layers, ${stats.tourSteps} tour steps`);
console.log(`validation issues=${issues.length} warnings=${warnings.length}`);

function countBy(items, field) {
  const out = {};
  for (const item of items) out[item[field]] = (out[item[field]] || 0) + 1;
  return out;
}
