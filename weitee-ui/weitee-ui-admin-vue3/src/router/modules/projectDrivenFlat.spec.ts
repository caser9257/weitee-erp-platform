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
      },
      {
        path: 'dual-ledger-config',
        name: '双账套账簿映射',
        component: 'erp/finance/dual-ledger-config/index',
        componentName: 'ErpFinanceDualLedgerConfig',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'dual-ledger-diff-config',
        name: '双账套口径配置',
        component: 'erp/finance/dual-ledger-diff-config/index',
        componentName: 'ErpFinanceDualLedgerDiffConfig',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'dual-ledger-result',
        name: '双账套结果查询',
        component: 'erp/finance/dual-ledger-result/index',
        componentName: 'ErpFinanceDualLedgerResult',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'dual-project-cost',
        name: '项目双账成本',
        component: 'erp/finance/dual-project-cost/index',
        componentName: 'ErpFinanceDualProjectCost',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'dual-product-cost',
        name: '产品双账成本',
        component: 'erp/finance/dual-product-cost/index',
        componentName: 'ErpFinanceDualProductCost',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'cost-report',
        name: '产品成本分析报表',
        component: 'erp/finance/cost-report/index',
        componentName: 'ErpFinanceCostReport',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'cost-product-trend',
        name: '产品成本趋势',
        component: 'erp/finance/cost-product/trend',
        componentName: 'ErpProductCostTrend',
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
    'subject',
    'period',
    'account',
    'ledger',
    'dual-ledger-config',
    'dual-ledger-diff-config',
    'report-item',
    'apar',
    'receipt',
    'payment',
    'prepayment',
    'ap-estimate',
    'ap-invoice',
    'expense',
    'voucher',
    'general-ledger',
    'assets',
    'voucher-template',
    'reports',
    'cost',
    'cost-product-trend',
    'dual-ledger-result',
    'dual-project-cost',
    'dual-product-cost',
    'cost-report',
    'project-cost'
  ]
)

const financeGroups = financeRoute?.children?.filter((item: any) => item.meta?.menuGroupKey) || []
assert.deepEqual(
  [...new Set(financeGroups.map((item: any) => item.meta?.menuGroupTitle))],
  ['基础设置', '往来与收付', '费用与采购管理', '总账与核算', '报表与分析']
)
assert.equal(financeGroups.find((item: any) => item.meta?.menuGroupKey === '/finance/__group__/basic-settings')?.path, 'subject')
assert.equal(financeGroups.find((item: any) => item.meta?.menuGroupKey === '/finance/__group__/expense-procurement')?.path, 'ap-estimate')
assert.equal(financeGroups.find((item: any) => item.path === 'ap-invoice')?.component, 'erp/finance/ap-invoice/index')
assert.equal(financeGroups.find((item: any) => item.path === 'expense')?.component, 'erp/finance/expense/index')
assert.equal(financeGroups.find((item: any) => item.meta?.menuGroupKey === '/finance/__group__/report-analysis')?.path, 'reports')
assert.equal(financeGroups.find((item: any) => item.path === 'dual-ledger-config')?.meta?.menuGroupKey, '/finance/__group__/basic-settings')
assert.equal(financeGroups.find((item: any) => item.path === 'dual-ledger-diff-config')?.meta?.menuGroupKey, '/finance/__group__/basic-settings')
assert.equal(financeGroups.find((item: any) => item.path === 'dual-ledger-result')?.meta?.menuGroupKey, '/finance/__group__/report-analysis')
assert.equal(financeGroups.find((item: any) => item.path === 'dual-project-cost')?.meta?.menuGroupKey, '/finance/__group__/report-analysis')
assert.equal(financeGroups.find((item: any) => item.path === 'dual-product-cost')?.meta?.menuGroupKey, '/finance/__group__/report-analysis')
assert.equal(financeGroups.find((item: any) => item.path === 'cost-report')?.meta?.menuGroupKey, '/finance/__group__/report-analysis')
assert.equal(financeGroups.find((item: any) => item.path === 'cost-product-trend')?.meta?.menuGroupKey, '/finance/__group__/report-analysis')
assert.deepEqual(
  financeRoute?.children
    ?.filter((item: any) => !item.meta?.menuGroupKey)
    .map((item: any) => item.path),
  ['project-cost']
)

const flatScmRemoteMenus = [
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

const flatScmMergedMenus = mergeProjectDrivenMenus(flatScmRemoteMenus)
const flatScmRoute = flatScmMergedMenus.find((item: any) => item.path === '/scm')

assert.equal(flatScmRoute?.path, '/scm')
assert.equal(flatScmRoute?.redirect, '/scm/suggest')
assert.deepEqual(
  flatScmRoute?.children?.map((item: any) => item.path),
  [
    'plan-rule',
    'plan',
    'suggest',
    'purchase-request',
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
    'substitute-material'
  ]
)

const scmGroups = flatScmRoute?.children?.filter((item: any) => item.meta?.menuGroupKey) || []
assert.deepEqual(
  [...new Set(scmGroups.map((item: any) => item.meta?.menuGroupTitle))],
  ['需求与计划', '采购执行', '库存与仓储']
)
assert.equal(scmGroups.find((item: any) => item.meta?.menuGroupKey === '/scm/__group__/demand-plan')?.path, 'plan-rule')
assert.equal(scmGroups.find((item: any) => item.path === 'plan-rule')?.name, '计划参数配置')
assert.equal(scmGroups.find((item: any) => item.path === 'suggest')?.name, 'MRP 运算中心')
assert.equal(scmGroups.find((item: any) => item.path === 'purchase-request')?.name, '采购需求汇总')
assert.equal(scmGroups.find((item: any) => item.path === 'stock-occupancy'), undefined)
assert.equal(scmGroups.find((item: any) => item.path === 'manufacture-bom'), undefined)
assert.equal(scmGroups.find((item: any) => item.meta?.menuGroupKey === '/scm/__group__/procurement')?.path, 'purchase-order')
assert.equal(scmGroups.find((item: any) => item.path === 'purchase-order')?.name, '采购订单台账')
assert.equal(scmGroups.find((item: any) => item.path === 'return')?.name, '采购退货')
assert.equal(scmGroups.find((item: any) => item.path === 'return')?.meta?.menuGroupKey, '/scm/__group__/procurement')
assert.equal(scmGroups.find((item: any) => item.path === 'outsource-inbound')?.name, '委外入库')
assert.equal(scmGroups.find((item: any) => item.meta?.menuGroupKey === '/scm/__group__/inventory-warehouse')?.path, 'stock-in')
assert.equal(scmGroups.find((item: any) => item.path === 'stock-out')?.name, '其他出库')
assert.equal(scmGroups.find((item: any) => item.path === 'warehouse')?.name, '仓库信息')
assert.equal(scmGroups.find((item: any) => item.path === 'stock')?.name, '即时库存查询')
assert.equal(scmGroups.find((item: any) => item.path === 'stock-record')?.name, '库存明细账')
assert.equal(scmGroups.find((item: any) => item.path === 'stock-analysis')?.name, '库存分析')
assert.equal(scmGroups.find((item: any) => item.path === 'netting-policy')?.meta?.menuGroupKey, '/scm/__group__/demand-plan')
assert.equal(scmGroups.find((item: any) => item.path === 'netting-policy')?.name, '净需求策略')
assert.equal(scmGroups.find((item: any) => item.path === 'substitute-material')?.name, '替代料台账')

const groupedScmRemoteMenus = [
  {
    path: '/scm',
    name: '供应链管理',
    component: '',
    componentName: 'FormalScmRoot',
    redirect: '',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    children: [
      {
        path: 'demand-plan',
        name: '需求与计划',
        component: '',
        componentName: 'FormalScmDemandPlanGroup',
        meta: {},
        visible: true,
        keepAlive: false,
        alwaysShow: true,
        children: [
          { path: 'plan-rule', name: '计划参数', component: 'erp/mrp/plan-rule/index', meta: {}, visible: true, keepAlive: false },
          { path: 'suggest', name: 'MRP 运算', component: 'erp/mrp/suggest/index', meta: {}, visible: true, keepAlive: false }
        ]
      },
      {
        path: 'procurement',
        name: '采购执行',
        component: '',
        componentName: 'FormalScmProcurementGroup',
        meta: {},
        visible: true,
        keepAlive: false,
        alwaysShow: true,
        children: [
          { path: 'purchase-order', name: '采购订单台账', component: 'erp/purchase/order/index', meta: {}, visible: true, keepAlive: false }
        ]
      },
      {
        path: 'inventory-warehouse',
        name: '库存与仓储',
        component: '',
        componentName: 'FormalScmInventoryWarehouseGroup',
        meta: {},
        visible: true,
        keepAlive: false,
        alwaysShow: true,
        children: [
          { path: 'stock-in', name: '其他入库', component: 'erp/stock/in/index', meta: {}, visible: true, keepAlive: false },
          { path: 'stock-occupancy', name: '库存占用追溯', component: 'erp/mrp/stock-reservation/index', meta: {}, visible: true, keepAlive: false }
        ]
      }
    ]
  }
] as any[]

const groupedScmMergedMenus = mergeProjectDrivenMenus(groupedScmRemoteMenus)
const groupedScmRoute = groupedScmMergedMenus.find((item: any) => item.path === '/scm')
assert.deepEqual(
  groupedScmRoute?.children?.filter((item: any) => item.visible !== false).map((item: any) => item.path),
  ['demand-plan', 'procurement', 'inventory-warehouse']
)
assert.deepEqual(
  groupedScmRoute?.children?.find((item: any) => item.path === 'inventory-warehouse')?.children?.map((item: any) => item.path),
  ['stock-in']
)
assert.equal(
  groupedScmRoute?.children?.find((item: any) => item.path === 'purchase-order')?.component,
  'erp/purchase/order/index'
)
assert.equal(
  groupedScmRoute?.children?.find((item: any) => item.path === 'purchase-order')?.meta?.hidden,
  true
)
assert.equal(
  groupedScmRoute?.children?.find((item: any) => item.path === 'procurement')?.children?.find((item: any) => item.path === 'purchase-order')?.component,
  'erp/purchase/order/index'
)

const supplyChainOnlyMenus = [
  {
    path: '/scm',
    name: '供应链管理',
    component: '',
    componentName: 'FormalScmRoot',
    redirect: '/scm/assemble',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    children: [
      {
        path: 'assemble',
        name: '组装与拆卸',
        component: 'erp/stock/assemble/index',
        componentName: 'ErpStockAssemblePage',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  }
] as any[]

const supplyChainOnlyMergedMenus = mergeProjectDrivenMenus(supplyChainOnlyMenus)
assert.equal(
  supplyChainOnlyMergedMenus.find((item: any) => item.path === '/finance'),
  undefined,
  '未被后端授权的财务根菜单不应由项目增强菜单注入'
)

const routerHelperSource = readFileSync(
  new URL('../../utils/routerHelper.ts', import.meta.url),
  'utf8'
)

assert.match(
  routerHelperSource,
  /const resolveRouteComponentPath = \(route: AppCustomRouteRecordRaw\) => \{[\s\S]*return 'common\/menu-placeholder\/index'/,
  'routerHelper.ts 应该在组件缺失时回退到占位页，而不是把 route.path 当成组件路径'
)
