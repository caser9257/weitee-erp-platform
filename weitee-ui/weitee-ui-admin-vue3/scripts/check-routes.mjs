import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const srcDir = path.resolve(__dirname, '../src')
const sqlDir = path.resolve(__dirname, '../sql/mysql')

const errors = []
const warnings = []

// ---------- 1. 收集 views 下所有可用组件 ----------
const viewFiles = new Set()
const walkViews = (dir) => {
  if (!fs.existsSync(dir)) return
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) walkViews(full)
    else if (/\.(vue|tsx)$/.test(entry.name)) {
      const rel = path.relative(srcDir, full).replace(/\\/g, '/')
      viewFiles.add(rel.replace(/^views\//, '').replace(/\.(vue|tsx)$/, ''))
      if (/^index\.(vue|tsx)$/.test(entry.name)) {
        viewFiles.add(path.relative(srcDir, path.dirname(full)).replace(/\\/g, '/'))
      }
    }
  }
}
walkViews(path.join(srcDir, 'views'))

const hasView = (componentPath) => viewFiles.has(componentPath.replace(/^\/+/, ''))

// ---------- 2. 从路由模块抽取 component 字符串 ----------
const routeFiles = [
  'router/modules/projectDrivenFlat.ts',
  'router/modules/remaining.ts'
]
const routeComponents = new Map() // component -> [locations]
for (const rel of routeFiles) {
  const full = path.join(srcDir, rel)
  if (!fs.existsSync(full)) continue
  const content = fs.readFileSync(full, 'utf8')
  const re = /component:\s*['"]([^'"]+)['"]/g
  let m
  while ((m = re.exec(content))) {
    const comp = m[1]
    if (!comp || comp === 'Layout' || comp.includes('menu-placeholder')) continue
    const line = content.slice(0, m.index).split('\n').length
    if (!routeComponents.has(comp)) routeComponents.set(comp, [])
    routeComponents.get(comp).push(`${rel}:${line}`)
  }
  // 静态 import 形式 () => import('@/views/xxx.vue')
  const re2 = /import\(['"]@\/views\/([^'"]+)['"]\)/g
  while ((m = re2.exec(content))) {
    const comp = m[1].replace(/\.(vue|tsx)$/, '')
    if (!routeComponents.has(comp)) routeComponents.set(comp, [])
    const line = content.slice(0, m.index).split('\n').length
    routeComponents.get(comp).push(`${rel}:${line} (static import)`)
  }
}

// ---------- 3. 从菜单种子 SQL 抽取 (path, component) ----------
const sqlComponents = new Map()
const walkSql = (dir) => {
  if (!fs.existsSync(dir)) return
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) walkSql(full)
    else if (/\.sql$/.test(entry.name)) {
      const content = fs.readFileSync(full, 'utf8')
      // INSERT INTO system_menu ... SELECT ..., 'component', ... 形式与 UPDATE component='x' 形式
      const re = /'((?:[a-zA-Z0-9_-]+\/)*[a-zA-Z0-9_-]+\/index)'/g
      let m
      while ((m = re.exec(content))) {
        const comp = m[1]
        if (!comp.includes('/')) continue
        if (!sqlComponents.has(comp)) sqlComponents.set(comp, [])
        const line = content.slice(0, m.index).split('\n').length
        sqlComponents.get(comp).push(`${path.relative(path.dirname(sqlDir), full).replace(/\\/g, '/')}:${line}`)
      }
    }
  }
}
walkSql(sqlDir)

// ---------- 4. 校验 ----------
console.log('\n=== 前端路由自检 ===\n')

let checked = 0
for (const [comp, locs] of routeComponents) {
  checked++
  if (!hasView(comp)) {
    errors.push(`路由组件不存在: "${comp}"  引用位置: ${locs.join(', ')}`)
  }
}
for (const [comp, locs] of sqlComponents) {
  checked++
  if (!hasView(comp)) {
    warnings.push(`菜单种子组件不存在(可能为历史遗留): "${comp}"  种子位置: ${locs.slice(0, 2).join(', ')}`)
  }
}

// formalRootPaths 覆盖检查
const menuRulePath = path.join(srcDir, 'utils/menuRouteRule.ts')
if (fs.existsSync(menuRulePath)) {
  const ruleContent = fs.readFileSync(menuRulePath, 'utf8')
  const flatPath = path.join(srcDir, 'router/modules/projectDrivenFlat.ts')
  const flatContent = fs.readFileSync(flatPath, 'utf8')
  const augRoots = new Set()
  const re = /path:\s*'(\/[a-z-]+)'/g
  let m
  // 只取 flatMenuAugmentations 之后的段落，简化：取全部顶级 path
  while ((m = re.exec(flatContent))) {
    const p = m[1]
    if (/^\/[a-z-]+$/.test(p) && p !== '/erp' && p !== '/scm' && p !== '/finance') augRoots.add(p)
  }
  for (const root of augRoots) {
    if (!ruleContent.includes(`'${root}'`)) {
      warnings.push(`formalRootPaths 可能缺少 "${root}"（projectDrivenFlat 中存在该顶级 path）`)
    }
  }
}

console.log(`已检查 ${checked} 个组件引用, views 下 ${viewFiles.size} 个可用组件\n`)

if (errors.length) {
  console.log('❌ 错误（阻断）:')
  errors.forEach((e) => console.log('  - ' + e))
}
if (warnings.length) {
  console.log('⚠️  警告:')
  warnings.forEach((w) => console.log('  - ' + w))
}
if (!errors.length && !warnings.length) {
  console.log('✅ 全部通过')
}

if (errors.length) {
  process.exit(1)
}
