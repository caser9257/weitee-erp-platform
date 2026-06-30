import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const { mergeProjectDrivenMenus } = await import(
  new URL('./projectDrivenFlat.ts', import.meta.url).href
)

const remoteMenus = [
  {
    path: '/finance',
    name: '财务管理',
    component: '',
    componentName: 'FormalFinanceRoot',
    redirect: '/finance/project-cost',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    children: [
      {
        path: 'project-cost',
        name: '项目成本',
        component: 'common/menu-placeholder/index',
        componentName: 'FormalFinanceProjectCost',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  }
] as any[]

const mergedMenus = mergeProjectDrivenMenus(remoteMenus)
const financeRoute = mergedMenus.find((item: any) => item.path === '/finance')

assert.equal(financeRoute?.path, '/finance')
assert.equal(financeRoute?.redirect, '/finance/project-cost')
assert.deepEqual(
  financeRoute?.children?.map((item: any) => item.path),
  [
    'project-cost',
    'subject',
    'period',
    'account',
    'ledger',
    'report-item',
    'apar',
    'receipt',
    'payment',
    'ap-estimate',
    'ap-invoice',
    'expense',
    'voucher',
    'assets',
    'reports',
    'cost'
  ]
)

const financeGroups = financeRoute?.children?.filter((item: any) => item.meta?.menuGroupKey) || []
assert.deepEqual(
  financeGroups.map((item: any) => item.meta?.menuGroupTitle),
  ['基础设置', '往来与收付', '费用与采购管理', '总账与核算', '报表与分析']
)
assert.equal(financeGroups.find((item: any) => item.meta?.menuGroupKey === '/finance/__group__/basic-settings')?.path, 'subject')
assert.equal(financeGroups.find((item: any) => item.meta?.menuGroupKey === '/finance/__group__/expense-procurement')?.path, 'ap-estimate')
assert.equal(financeGroups.find((item: any) => item.path === 'ap-invoice')?.component, 'erp/finance/ap-invoice/index')
assert.equal(financeGroups.find((item: any) => item.path === 'expense')?.component, 'erp/finance/expense/index')
assert.equal(financeGroups.find((item: any) => item.meta?.menuGroupKey === '/finance/__group__/report-analysis')?.path, 'reports')

const scmRemoteMenus = [
  {
    path: '/scm',
    name: '供应链管理',
    component: '',
    componentName: 'ProjectScmRoot',
    redirect: '/scm/suggest',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    children: [
      { path: 'plan-rule', name: '计划参数', component: 'erp/mrp/plan-rule/index', meta: {}, visible: true, keepAlive: false },
      { path: 'plan', name: 'MRP 计划', component: 'erp/mrp/plan/index', meta: {}, visible: true, keepAlive: false },
      { path: 'suggest', name: 'MRP 运算', component: 'erp/mrp/suggest/index', meta: {}, visible: true, keepAlive: false },
      { path: 'purchase-request', name: '采购需求', component: 'erp/mrp/suggest/index', meta: {}, visible: true, keepAlive: false },
      { path: 'stock-occupancy', name: '库存占用追溯', component: 'erp/mrp/stock-reservation/index', meta: {}, visible: true, keepAlive: false },
      { path: 'purchase-order', name: '采购订单台账', component: 'erp/purchase/order/index', meta: {}, visible: true, keepAlive: false },
      { path: 'inbound', name: '收货入库', component: 'erp/purchase/in/index', meta: {}, visible: true, keepAlive: false },
      { path: 'return', name: '采购退货', component: 'erp/purchase/return/index', meta: {}, visible: true, keepAlive: false },
      { path: 'warehouse', name: '仓库信息', component: 'erp/stock/warehouse/index', meta: {}, visible: true, keepAlive: false },
      { path: 'stock', name: '产品库存', component: 'erp/stock/stock/index', meta: {}, visible: true, keepAlive: false },
      { path: 'stock-record', name: '库存明细', component: 'erp/stock/record/index', meta: {}, visible: true, keepAlive: false },
      { path: 'stock-analysis', name: '库存分析', component: 'erp/stock/analysis/index', meta: {}, visible: true, keepAlive: false },
      { path: 'stock-in', name: '其他入库', component: 'erp/stock/in/index', meta: {}, visible: true, keepAlive: false },
      { path: 'stock-out', name: '其他出库', component: 'erp/stock/out/index', meta: {}, visible: true, keepAlive: false },
      { path: 'stock-move', name: '库存调拨', component: 'erp/stock/move/index', meta: {}, visible: true, keepAlive: false },
      { path: 'stock-check', name: '库存盘点', component: 'erp/stock/check/index', meta: {}, visible: true, keepAlive: false },
      { path: 'assemble', name: '组装与拆卸', component: 'erp/stock/assemble/index', meta: {}, visible: true, keepAlive: false },
      { path: 'netting-policy', name: '净需求策略', component: 'erp/mrp/netting-policy/index', meta: {}, visible: true, keepAlive: false },
      { path: 'substitute-material', name: '替代料台账', component: 'erp/mrp/substitute/index', meta: {}, visible: true, keepAlive: false },
      { path: 'manufacture-bom', name: 'BOM', component: 'erp/mrp/bom/index', meta: {}, visible: true, keepAlive: false }
    ]
  }
] as any[]

const scmMergedMenus = mergeProjectDrivenMenus(scmRemoteMenus)
const scmRoute = scmMergedMenus.find((item: any) => item.path === '/scm')

assert.equal(scmRoute?.path, '/scm')
assert.equal(scmRoute?.redirect, '/scm/suggest')
assert.deepEqual(
  scmRoute?.children?.map((item: any) => item.path),
  [
    'plan-rule',
    'plan',
    'suggest',
    'purchase-request',
    'stock-occupancy',
    'manufacture-bom',
    'purchase-order',
    'inbound',
    'outsource-inbound',
    'return',
    'stock-in',
    'stock-out',
    'stock-move',
    'stock-check',
    'assemble',
    'warehouse',
    'stock',
    'stock-record',
    'stock-analysis',
    'netting-policy',
    'substitute-material',
    'manufacture-bom'
  ]
)

const scmGroups = scmRoute?.children?.filter((item: any) => item.meta?.menuGroupKey) || []
assert.deepEqual(
  scmGroups.map((item: any) => item.meta?.menuGroupTitle),
  ['需求与计划', '采购执行', '仓储作业', '库存查询与基础资料']
)
assert.equal(scmGroups.find((item: any) => item.meta?.menuGroupKey === '/scm/__group__/demand-plan')?.path, 'plan-rule')
assert.equal(scmGroups.find((item: any) => item.path === 'plan-rule')?.name, '计划参数配置')
assert.equal(scmGroups.find((item: any) => item.path === 'suggest')?.name, 'MRP 运算中心')
assert.equal(scmGroups.find((item: any) => item.path === 'purchase-request')?.name, '采购需求汇总')
assert.equal(scmGroups.find((item: any) => item.path === 'manufacture-bom')?.name, '制造BOM')
assert.equal(scmGroups.find((item: any) => item.path === 'manufacture-bom')?.meta?.menuGroupKey, '/scm/__group__/demand-plan')
assert.equal(scmGroups.find((item: any) => item.meta?.menuGroupKey === '/scm/__group__/procurement')?.path, 'purchase-order')
assert.equal(scmGroups.find((item: any) => item.path === 'purchase-order')?.name, '采购订单台账')
assert.equal(scmGroups.find((item: any) => item.path === 'return')?.name, '采购退货')
assert.equal(scmGroups.find((item: any) => item.path === 'return')?.meta?.menuGroupKey, '/scm/__group__/procurement')
assert.equal(scmGroups.find((item: any) => item.path === 'outsource-inbound')?.name, '委外入库')
assert.equal(scmGroups.find((item: any) => item.meta?.menuGroupKey === '/scm/__group__/warehouse')?.path, 'stock-in')
assert.equal(scmGroups.find((item: any) => item.path === 'stock-out')?.name, '其他出库')
assert.equal(scmGroups.find((item: any) => item.path === 'warehouse')?.name, '仓库信息')
assert.equal(scmGroups.find((item: any) => item.path === 'stock')?.name, '即时库存查询')
assert.equal(scmGroups.find((item: any) => item.path === 'stock-record')?.name, '库存明细账')
assert.equal(scmGroups.find((item: any) => item.path === 'stock-analysis')?.name, '库存分析')
assert.equal(scmGroups.find((item: any) => item.meta?.menuGroupKey === '/scm/__group__/strategy-base')?.path, 'netting-policy')
assert.equal(scmGroups.find((item: any) => item.path === 'netting-policy')?.name, '净需求策略')
assert.equal(scmGroups.find((item: any) => item.path === 'substitute-material')?.name, '替代料台账')

const routerHelperSource = readFileSync(
  new URL('../../utils/routerHelper.ts', import.meta.url),
  'utf8'
)

assert.match(
  routerHelperSource,
  /const resolveRouteComponentPath = \(route: AppCustomRouteRecordRaw\) => \{[\s\S]*return 'common\/menu-placeholder\/index'/,
  'routerHelper.ts 应该在组件缺失时回退到占位页，而不是把 route.path 当成组件路径'
)
