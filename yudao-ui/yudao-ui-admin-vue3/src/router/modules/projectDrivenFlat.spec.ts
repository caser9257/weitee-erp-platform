import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const projectDrivenFlatSource = readFileSync(new URL('./projectDrivenFlat.ts', import.meta.url), 'utf8')
const routerHelperSource = readFileSync(
  new URL('../../utils/routerHelper.ts', import.meta.url),
  'utf8'
)

assert.match(
  projectDrivenFlatSource,
  /'outsource-inbound'[\s\S]*menuGroupKey:\s*'\/scm\/__group__\/procurement'/,
  'projectDrivenFlat.ts 应该保留委外入库在采购执行分组中的装饰规则'
)

assert.match(
  projectDrivenFlatSource,
  /'production-inbound'[\s\S]*menuGroupKey:\s*'\/scm\/__group__\/procurement'[\s\S]*menuOrder:\s*35[\s\S]*routeName:\s*'自制入库'/,
  'projectDrivenFlat.ts 应该为自制入库提供采购执行分组装饰规则'
)

assert.match(
  projectDrivenFlatSource,
  /path:\s*'outsource-inbound'[\s\S]*component:\s*'erp\/mrp\/outsource-inbound\/index'[\s\S]*componentName:\s*'ProjectScmOutsourceInbound'/,
  'projectDrivenFlat.ts 应该提供委外入库本地兜底菜单，避免远端菜单缺项时入口消失'
)

assert.match(
  projectDrivenFlatSource,
  /path:\s*'production-inbound'[\s\S]*component:\s*'erp\/mrp\/production-inbound\/index'[\s\S]*componentName:\s*'ProjectScmProductionInbound'/,
  'projectDrivenFlat.ts 应该提供自制入库本地兜底菜单，确保 SCM 菜单树能挂出入口'
)

assert.match(
  projectDrivenFlatSource,
  /path:\s*'general-ledger'[\s\S]*component:\s*'erp\/finance\/general-ledger\/index'[\s\S]*componentName:\s*'ErpFinanceGeneralLedger'[\s\S]*menuGroupKey:\s*'\/finance\/__group__\/ledger-accounting'/,
  'projectDrivenFlat.ts 应该提供总账本地兜底菜单，避免总账页面存在但路由入口缺失'
)

assert.match(
  projectDrivenFlatSource,
  /path:\s*'voucher-template'[\s\S]*component:\s*'erp\/finance\/voucher-template\/index'[\s\S]*componentName:\s*'ErpFinanceVoucherTemplate'[\s\S]*menuGroupKey:\s*'\/finance\/__group__\/ledger-accounting'/,
  'projectDrivenFlat.ts 应该提供凭证模板本地兜底菜单，避免页面存在但菜单入口缺失'
)

assert.match(
  projectDrivenFlatSource,
  /path:\s*'prepayment'[\s\S]*component:\s*'erp\/finance\/prepayment\/index'[\s\S]*componentName:\s*'ErpFinancePrepayment'[\s\S]*menuGroupKey:\s*'\/finance\/__group__\/receivables-payables'/,
  'projectDrivenFlat.ts 应该提供预付款本地兜底菜单，避免预付款页面存在但入口缺失'
)

assert.match(
  projectDrivenFlatSource,
  /path:\s*'dual-ledger-config'[\s\S]*component:\s*'erp\/finance\/dual-ledger-config\/index'[\s\S]*componentName:\s*'ErpFinanceDualLedgerConfig'[\s\S]*menuGroupKey:\s*'\/finance\/__group__\/basic-settings'/,
  'projectDrivenFlat.ts 应该提供双账套账簿映射本地兜底菜单，避免配置页存在但入口缺失'
)

assert.match(
  projectDrivenFlatSource,
  /path:\s*'dual-ledger-diff-config'[\s\S]*component:\s*'erp\/finance\/dual-ledger-diff-config\/index'[\s\S]*componentName:\s*'ErpFinanceDualLedgerDiffConfig'[\s\S]*menuGroupKey:\s*'\/finance\/__group__\/basic-settings'/,
  'projectDrivenFlat.ts 应该提供双账套口径配置本地兜底菜单，避免配置页存在但入口缺失'
)

assert.match(
  routerHelperSource,
  /const resolveRouteComponentPath = \(route: AppCustomRouteRecordRaw\) => \{[\s\S]*return 'common\/menu-placeholder\/index'/,
  'routerHelper.ts 应该在组件缺失时回退到占位页，而不是把 route.path 当成组件路径'
)
