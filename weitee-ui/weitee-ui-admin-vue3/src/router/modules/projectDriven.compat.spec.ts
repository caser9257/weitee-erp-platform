import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const projectDrivenSource = readFileSync(new URL('./projectDriven.ts', import.meta.url), 'utf8')

assert.match(
  projectDrivenSource,
  /path:\s*'closure-workbench'[\s\S]*component:\s*'erp\/sale\/order\/closure-workbench'/,
  'projectDriven.ts 应该提供 /sales/closure-workbench 正式菜单，供销售闭环工作台从主菜单进入'
)

assert.match(
  projectDrivenSource,
  /path:\s*'closure-workbench'[\s\S]*componentName:\s*'ProjectSalesClosureWorkbench'/,
  '销售闭环工作台菜单应提供稳定的 componentName，避免菜单与缓存行为漂移'
)

assert.match(
  projectDrivenSource,
  /path:\s*'warning'[\s\S]*component:\s*'pmo\/project\/warning\/index'/,
  'projectDriven.ts 应该提供项目预警正式页面，避免继续落到占位页'
)

assert.match(
  projectDrivenSource,
  /path:\s*'warning'[\s\S]*name:\s*'项目预警'[\s\S]*componentName:\s*'ProjectPmoWarning'/,
  'projectDriven.ts 应该保留项目预警菜单入口'
)

assert.match(
  projectDrivenSource,
  /path:\s*'ar-statement'[\s\S]*component:\s*'erp\/finance\/ar-statement\/index'[\s\S]*componentName:\s*'ErpArStatement'/,
  'projectDriven.ts 应该提供应收台账页面入口'
)
