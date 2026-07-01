import assert from 'node:assert/strict'

import { buildGroupedMenuEntries, hasGroupedMenuEntries, resolveGroupedMenuActiveKey } from './groupedMenu.ts'

const routes = [
  {
    path: '/finance',
    meta: {},
    children: [
      {
        path: 'subject',
        meta: {
          menuGroupKey: '/finance/__group__/basic-settings',
          menuGroupTitle: '基础设置',
          menuGroupIcon: 'ep:setting',
          menuGroupOrder: 10,
          menuOrder: 10
        }
      },
      {
        path: 'ledger',
        meta: {
          menuGroupKey: '/finance/__group__/ledger-accounting',
          menuGroupTitle: '总账与核算',
          menuGroupIcon: 'ep:collection',
          menuGroupOrder: 40,
          menuOrder: 10
        }
      }
    ]
  },
  {
    path: '/scm/plan-rule',
    meta: {
      menuGroupKey: '/scm/__group__/demand-plan',
      menuGroupTitle: '需求计划',
      menuGroupIcon: 'ep:histogram',
      menuGroupOrder: 10,
      menuOrder: 20
    }
  },
  {
    path: '/scm/suggest',
    meta: {
      menuGroupKey: '/scm/__group__/demand-plan',
      menuGroupTitle: '需求计划',
      menuGroupIcon: 'ep:histogram',
      menuGroupOrder: 10,
      menuOrder: 10
    }
  },
  {
    path: '/scm/warehouse',
    meta: {
      menuGroupKey: '/scm/__group__/warehouse',
      menuGroupTitle: '仓储作业',
      menuGroupIcon: 'ep:box',
      menuGroupOrder: 30,
      menuOrder: 10
    }
  },
  {
    path: '/scm/netting-policy',
    meta: {
      menuGroupKey: '/scm/__group__/strategy-base',
      menuGroupTitle: '策略与基础',
      menuGroupIcon: 'ep:setting',
      menuGroupOrder: 40,
      menuOrder: 10
    }
  }
] as any[]

const { groups, directRoutes } = buildGroupedMenuEntries(routes)

assert.equal(hasGroupedMenuEntries(routes), true)
assert.deepEqual(
  groups.map((group) => group.key),
  [
    '/finance/__group__/basic-settings',
    '/scm/__group__/demand-plan',
    '/scm/__group__/warehouse',
    '/finance/__group__/ledger-accounting',
    '/scm/__group__/strategy-base'
  ]
)
assert.deepEqual(
  groups.find((group) => group.key === '/scm/__group__/demand-plan')?.routes.map((route) => route.path),
  ['/scm/suggest', '/scm/plan-rule']
)
assert.deepEqual(directRoutes.map((route) => route.path), [])

assert.equal(
  resolveGroupedMenuActiveKey({
    groups,
    activeMenu: '/finance/ledger',
    currentPath: '/finance/ledger'
  }),
  '/finance/__group__/ledger-accounting'
)

assert.equal(
  resolveGroupedMenuActiveKey({
    groups,
    activeMenu: '/scm/warehouse',
    currentPath: '/scm/warehouse',
    routeMetaGroupKey: '/scm/__group__/warehouse'
  }),
  '/scm/__group__/warehouse'
)

const databaseGroupedRoutes = [
  {
    path: '/scm',
    name: '供应链管理',
    meta: { title: '供应链管理', icon: 'ep:shopping-cart-full' },
    children: [
      {
        path: 'demand-plan',
        name: '需求与计划',
        component: {},
        meta: { title: '需求与计划', icon: 'ep:histogram', order: 10 },
        children: [
          { path: 'plan-rule', name: '计划参数', meta: { title: '计划参数' } },
          { path: 'suggest', name: 'MRP 运算', meta: { title: 'MRP 运算' } }
        ]
      },
      {
        path: 'procurement',
        name: '采购执行',
        component: {},
        meta: { title: '采购执行', icon: 'ep:shopping-cart-full', order: 20 },
        children: [
          { path: 'purchase-order', name: '采购订单台账', meta: { title: '采购订单台账' } },
          { path: 'inbound', name: '收货入库', meta: { title: '收货入库' } },
          { path: 'outsource-inbound', name: '委外入库', meta: { title: '委外入库' } },
          { path: 'return', name: '采购退货', meta: { title: '采购退货' } }
        ]
      }
    ]
  }
] as any[]

const databaseGroupedModel = buildGroupedMenuEntries(databaseGroupedRoutes)

assert.equal(hasGroupedMenuEntries(databaseGroupedRoutes), true)
assert.deepEqual(
  databaseGroupedModel.groups.map((group) => group.key),
  ['/scm/demand-plan', '/scm/procurement']
)
assert.deepEqual(
  databaseGroupedModel.groups.find((group) => group.key === '/scm/procurement')?.routes.map((route) => route.path),
  ['purchase-order', 'inbound', 'outsource-inbound', 'return']
)
assert.deepEqual(
  databaseGroupedModel.directRoutes.map((route) => route.path),
  []
)
