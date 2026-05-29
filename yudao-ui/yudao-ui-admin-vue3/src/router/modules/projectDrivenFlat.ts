import { formalRootPaths } from '@/utils/menuRouteRule'
import { hasViewModule } from '@/utils/viewModuleResolver'

const cloneRouteTree = (route: AppCustomRouteRecordRaw): AppCustomRouteRecordRaw => {
  return {
    ...route,
    meta: { ...(route.meta || {}) },
    children: route.children?.map((child) => cloneRouteTree(child))
  }
}

const normalizeRoutePath = (path?: string) => {
  if (!path) {
    return ''
  }
  const normalized = path.replace(/\/+/g, '/')
  if (normalized.length > 1 && normalized.endsWith('/')) {
    return normalized.slice(0, -1)
  }
  return normalized
}

const normalizeRouteSegment = (path?: string) => {
  return normalizeRoutePath(path).replace(/^\/+/, '')
}

const normalizeComponentPath = (component?: string) => {
  return normalizeRoutePath(component)
}

const isPlaceholderComponent = (component?: string) => {
  if (!component) {
    return false
  }
  return component === 'common/menu-placeholder/index' || component.includes('menu-placeholder')
}

const resolveMergedComponent = (remoteComponent?: string, localComponent?: string) => {
  const normalizedRemoteComponent = normalizeComponentPath(remoteComponent)
  if (
    normalizedRemoteComponent &&
    !isPlaceholderComponent(normalizedRemoteComponent) &&
    hasViewModule(normalizedRemoteComponent)
  ) {
    return remoteComponent
  }

  if (localComponent) {
    return localComponent
  }

  return remoteComponent
}

const duplicatedLegacyMenuComponents = new Set(['erp/purchase/in-quality/index'])
const duplicatedLegacyMenuChains = new Set(['purchase/in-quality'])
const absorbedLegacyRootPaths = new Set(['/manufacturing', '/mrp'])
const legacyMenuSourceMetaKey = '__legacyMenuSource'
const legacyMenuSourceErp = 'erp'

type ScmMenuDecoration = {
  menuGroupKey: string
  menuGroupTitle: string
  menuGroupIcon: string
  menuGroupOrder: number
  menuOrder: number
  routeName?: string
}

const scmMenuDecorations = new Map<string, ScmMenuDecoration>([
  [
    'plan-rule',
    {
      menuGroupKey: '/scm/__group__/demand-plan',
      menuGroupTitle: '需求与计划',
      menuGroupIcon: 'ep:histogram',
      menuGroupOrder: 10,
      menuOrder: 10,
      routeName: '计划参数配置'
    }
  ],
  [
    'plan',
    {
      menuGroupKey: '/scm/__group__/demand-plan',
      menuGroupTitle: '需求与计划',
      menuGroupIcon: 'ep:histogram',
      menuGroupOrder: 10,
      menuOrder: 20,
      routeName: 'MRP 计划'
    }
  ],
  [
    'suggest',
    {
      menuGroupKey: '/scm/__group__/demand-plan',
      menuGroupTitle: '需求与计划',
      menuGroupIcon: 'ep:histogram',
      menuGroupOrder: 10,
      menuOrder: 30,
      routeName: 'MRP 运算中心'
    }
  ],
  [
    'purchase-request',
    {
      menuGroupKey: '/scm/__group__/demand-plan',
      menuGroupTitle: '需求与计划',
      menuGroupIcon: 'ep:histogram',
      menuGroupOrder: 10,
      menuOrder: 40,
      routeName: '采购需求汇总'
    }
  ],
  [
    'stock-occupancy',
    {
      menuGroupKey: '/scm/__group__/demand-plan',
      menuGroupTitle: '需求与计划',
      menuGroupIcon: 'ep:histogram',
      menuGroupOrder: 10,
      menuOrder: 50,
      routeName: '库存占用追溯'
    }
  ],
  [
    'manufacture-bom',
    {
      menuGroupKey: '/scm/__group__/demand-plan',
      menuGroupTitle: '需求与计划',
      menuGroupIcon: 'ep:histogram',
      menuGroupOrder: 10,
      menuOrder: 60,
      routeName: '制造BOM'
    }
  ],
  [
    'purchase-order',
    {
      menuGroupKey: '/scm/__group__/procurement',
      menuGroupTitle: '采购执行',
      menuGroupIcon: 'ep:shopping-cart-full',
      menuGroupOrder: 20,
      menuOrder: 10,
      routeName: '采购订单台账'
    }
  ],
  [
    'inbound',
    {
      menuGroupKey: '/scm/__group__/procurement',
      menuGroupTitle: '采购执行',
      menuGroupIcon: 'ep:shopping-cart-full',
      menuGroupOrder: 20,
      menuOrder: 20,
      routeName: '收货入库'
    }
  ],
  [
    'outsource-inbound',
    {
      menuGroupKey: '/scm/__group__/procurement',
      menuGroupTitle: '采购执行',
      menuGroupIcon: 'ep:shopping-cart-full',
      menuGroupOrder: 20,
      menuOrder: 30,
      routeName: '委外入库'
    }
  ],
  [
    'production-inbound',
    {
      menuGroupKey: '/scm/__group__/procurement',
      menuGroupTitle: '閲囪喘鎵ц',
      menuGroupIcon: 'ep:shopping-cart-full',
      menuGroupOrder: 20,
      menuOrder: 35,
      routeName: '自制入库'
    }
  ],
  [
    'return',
    {
      menuGroupKey: '/scm/__group__/procurement',
      menuGroupTitle: '采购执行',
      menuGroupIcon: 'ep:shopping-cart-full',
      menuGroupOrder: 20,
      menuOrder: 40,
      routeName: '采购退货'
    }
  ],
  [
    'stock-in',
    {
      menuGroupKey: '/scm/__group__/warehouse',
      menuGroupTitle: '仓储作业',
      menuGroupIcon: 'ep:box',
      menuGroupOrder: 30,
      menuOrder: 10,
      routeName: '其他入库'
    }
  ],
  [
    'stock-out',
    {
      menuGroupKey: '/scm/__group__/warehouse',
      menuGroupTitle: '仓储作业',
      menuGroupIcon: 'ep:box',
      menuGroupOrder: 30,
      menuOrder: 20,
      routeName: '其他出库'
    }
  ],
  [
    'stock-move',
    {
      menuGroupKey: '/scm/__group__/warehouse',
      menuGroupTitle: '仓储作业',
      menuGroupIcon: 'ep:box',
      menuGroupOrder: 30,
      menuOrder: 30,
      routeName: '库存调拨'
    }
  ],
  [
    'stock-check',
    {
      menuGroupKey: '/scm/__group__/warehouse',
      menuGroupTitle: '仓储作业',
      menuGroupIcon: 'ep:box',
      menuGroupOrder: 30,
      menuOrder: 40,
      routeName: '库存盘点'
    }
  ],
  [
    'assemble',
    {
      menuGroupKey: '/scm/__group__/warehouse',
      menuGroupTitle: '仓储作业',
      menuGroupIcon: 'ep:box',
      menuGroupOrder: 30,
      menuOrder: 50,
      routeName: '组装与拆卸'
    }
  ],
  [
    'warehouse',
    {
      menuGroupKey: '/scm/__group__/strategy-base',
      menuGroupTitle: '库存查询与基础资料',
      menuGroupIcon: 'ep:setting',
      menuGroupOrder: 40,
      menuOrder: 10,
      routeName: '仓库信息'
    }
  ],
  [
    'warehouse-category',
    {
      menuGroupKey: '/scm/__group__/strategy-base',
      menuGroupTitle: '库存查询与基础资料',
      menuGroupIcon: 'ep:setting',
      menuGroupOrder: 40,
      menuOrder: 15,
      routeName: '仓库分类管理'
    }
  ],
  [
    'stock',
    {
      menuGroupKey: '/scm/__group__/strategy-base',
      menuGroupTitle: '库存查询与基础资料',
      menuGroupIcon: 'ep:setting',
      menuGroupOrder: 40,
      menuOrder: 20,
      routeName: '即时库存查询'
    }
  ],
  [
    'stock-record',
    {
      menuGroupKey: '/scm/__group__/strategy-base',
      menuGroupTitle: '库存查询与基础资料',
      menuGroupIcon: 'ep:setting',
      menuGroupOrder: 40,
      menuOrder: 30,
      routeName: '库存明细账'
    }
  ],
  [
    'stock-analysis',
    {
      menuGroupKey: '/scm/__group__/strategy-base',
      menuGroupTitle: '库存查询与基础资料',
      menuGroupIcon: 'ep:setting',
      menuGroupOrder: 40,
      menuOrder: 40,
      routeName: '库存分析'
    }
  ],
  [
    'netting-policy',
    {
      menuGroupKey: '/scm/__group__/strategy-base',
      menuGroupTitle: '库存查询与基础资料',
      menuGroupIcon: 'ep:setting',
      menuGroupOrder: 40,
      menuOrder: 50,
      routeName: '净需求策略'
    }
  ],
  [
    'substitute-material',
    {
      menuGroupKey: '/scm/__group__/strategy-base',
      menuGroupTitle: '库存查询与基础资料',
      menuGroupIcon: 'ep:setting',
      menuGroupOrder: 40,
      menuOrder: 60,
      routeName: '替代料台账'
    }
  ]
])

const sortByOrder = <T extends { meta?: Record<string, any> }>(routes: T[] = []) => {
  return [...routes].sort((left, right) => {
    const leftGroupOrder = Number(left.meta?.menuGroupOrder ?? Number.MAX_SAFE_INTEGER)
    const rightGroupOrder = Number(right.meta?.menuGroupOrder ?? Number.MAX_SAFE_INTEGER)
    if (leftGroupOrder !== rightGroupOrder) {
      return leftGroupOrder - rightGroupOrder
    }

    const leftMenuOrder = Number(left.meta?.menuOrder ?? Number.MAX_SAFE_INTEGER)
    const rightMenuOrder = Number(right.meta?.menuOrder ?? Number.MAX_SAFE_INTEGER)
    if (leftMenuOrder !== rightMenuOrder) {
      return leftMenuOrder - rightMenuOrder
    }

    return 0
  })
}

const decorateScmMenus = (routes: AppCustomRouteRecordRaw[] = []) => {
  return routes.map((route) => {
    const clonedRoute = cloneRouteTree(route)
    if (normalizeRoutePath(clonedRoute.path) !== '/scm') {
      return clonedRoute
    }

    clonedRoute.children = sortByOrder(
      (clonedRoute.children || []).map((child) => {
        const decoratedChild = cloneRouteTree(child)
        const decoration = scmMenuDecorations.get(normalizeRouteSegment(decoratedChild.path))
        if (!decoration) {
          return decoratedChild
        }

        decoratedChild.meta = {
          ...(decoratedChild.meta || {}),
          menuGroupKey: decoration.menuGroupKey,
          menuGroupTitle: decoration.menuGroupTitle,
          menuGroupIcon: decoration.menuGroupIcon,
          menuGroupOrder: decoration.menuGroupOrder,
          menuOrder: decoration.menuOrder
        }

        if (decoration.routeName) {
          decoratedChild.name = decoration.routeName
        }

        return decoratedChild
      })
    )

    return clonedRoute
  })
}

const flatMenuAugmentations: AppCustomRouteRecordRaw[] = [
  {
    path: '/erp',
    icon: '',
    name: 'ERP',
    component: '',
    componentName: 'ProjectErpRoot',
    redirect: '',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    children: [
      {
        path: 'finance',
        name: '财务管理',
        icon: 'ep:money',
        component: '',
        componentName: 'ProjectFinanceRoot',
        redirect: '/finance/project-cost',
        meta: {},
        visible: true,
        keepAlive: false,
        alwaysShow: true,
        children: [
          {
            path: 'subject',
            name: '财务科目',
            icon: 'ep:document',
            component: 'erp/finance/subject/index',
            componentName: 'ErpFinanceSubject',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/basic-settings',
              menuGroupTitle: '基础设置',
              menuGroupIcon: 'ep:setting',
              menuGroupOrder: 10,
              menuOrder: 10
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'period',
            name: '会计期间',
            icon: 'ep:calendar',
            component: 'erp/finance/period/index',
            componentName: 'ErpFinancePeriod',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/basic-settings',
              menuGroupTitle: '基础设置',
              menuGroupIcon: 'ep:setting',
              menuGroupOrder: 10,
              menuOrder: 20
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'account',
            name: '结算账户',
            icon: 'fa:universal-access',
            component: 'erp/finance/account/index',
            componentName: 'ErpAccount',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/basic-settings',
              menuGroupTitle: '基础设置',
              menuGroupIcon: 'ep:setting',
              menuGroupOrder: 10,
              menuOrder: 30
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'ledger',
            name: '财务账簿',
            icon: 'ep:collection',
            component: 'erp/finance/ledger/index',
            componentName: 'ErpFinanceLedger',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/basic-settings',
              menuGroupTitle: '基础设置',
              menuGroupIcon: 'ep:setting',
              menuGroupOrder: 10,
              menuOrder: 40
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'dual-ledger-config',
            name: '双账套账簿映射',
            icon: 'ep:connection',
            component: 'erp/finance/dual-ledger-config/index',
            componentName: 'ErpFinanceDualLedgerConfig',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/basic-settings',
              menuGroupTitle: '基础设置',
              menuGroupIcon: 'ep:setting',
              menuGroupOrder: 10,
              menuOrder: 45
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'dual-ledger-diff-config',
            name: '双账套口径配置',
            icon: 'ep:operation',
            component: 'erp/finance/dual-ledger-diff-config/index',
            componentName: 'ErpFinanceDualLedgerDiffConfig',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/basic-settings',
              menuGroupTitle: '基础设置',
              menuGroupIcon: 'ep:setting',
              menuGroupOrder: 10,
              menuOrder: 46
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'report-item',
            name: '报表项目',
            icon: 'ep:list',
            component: 'erp/finance/report-item/index',
            componentName: 'ErpFinanceReportItem',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/basic-settings',
              menuGroupTitle: '基础设置',
              menuGroupIcon: 'ep:setting',
              menuGroupOrder: 10,
              menuOrder: 50
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'apar',
            name: '应收应付账款池',
            icon: 'ep:wallet',
            component: 'erp/finance/apar/index',
            componentName: 'ProjectFinanceApar',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/receivables-payables',
              menuGroupTitle: '往来与收付',
              menuGroupIcon: 'ep:wallet',
              menuGroupOrder: 20,
              menuOrder: 10
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'receipt',
            name: '项目收款管理',
            icon: 'ep:expand',
            component: 'erp/finance/receipt/index',
            componentName: 'ErpFinanceReceipt',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/receivables-payables',
              menuGroupTitle: '往来与收付',
              menuGroupIcon: 'ep:wallet',
              menuGroupOrder: 20,
              menuOrder: 20
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'payment',
            name: '项目付款管理',
            icon: 'ep:caret-right',
            component: 'erp/finance/payment/index',
            componentName: 'ErpFinancePayment',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/receivables-payables',
              menuGroupTitle: '往来与收付',
              menuGroupIcon: 'ep:wallet',
              menuGroupOrder: 20,
              menuOrder: 30
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'prepayment',
            name: '预付款',
            icon: 'ep:wallet-filled',
            component: 'erp/finance/prepayment/index',
            componentName: 'ErpFinancePrepayment',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/receivables-payables',
              menuGroupTitle: '往来与收付',
              menuGroupIcon: 'ep:wallet',
              menuGroupOrder: 20,
              menuOrder: 35
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'ap-estimate',
            name: '采购暂估入库',
            icon: 'ep:clock',
            component: 'erp/finance/ap-estimate/index',
            componentName: 'ProjectFinanceApEstimate',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/expense-procurement',
              menuGroupTitle: '费用与采购管理',
              menuGroupIcon: 'ep:document-checked',
              menuGroupOrder: 30,
              menuOrder: 10
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'ap-invoice',
            name: '采购发票匹配',
            icon: 'ep:document-checked',
            component: 'erp/finance/ap-invoice/index',
            componentName: 'ErpApInvoice',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/expense-procurement',
              menuGroupTitle: '费用与采购管理',
              menuGroupIcon: 'ep:document-checked',
              menuGroupOrder: 30,
              menuOrder: 20
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'expense',
            name: '研发报销 / 零星采购',
            icon: 'ep:document',
            component: 'erp/finance/expense/index',
            componentName: 'FormalFinanceExpense',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/expense-procurement',
              menuGroupTitle: '费用与采购管理',
              menuGroupIcon: 'ep:document-checked',
              menuGroupOrder: 30,
              menuOrder: 30
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'voucher',
            name: '财务凭证',
            icon: 'ep:tickets',
            component: 'erp/finance/voucher/index',
            componentName: 'ErpFinanceVoucher',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/ledger-accounting',
              menuGroupTitle: '总账与核算',
              menuGroupIcon: 'ep:collection',
              menuGroupOrder: 40,
              menuOrder: 10
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'voucher-template',
            name: '凭证模板',
            icon: 'ep:files',
            component: 'erp/finance/voucher-template/index',
            componentName: 'ErpFinanceVoucherTemplate',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/ledger-accounting',
              menuGroupTitle: '总账与核算',
              menuGroupIcon: 'ep:collection',
              menuGroupOrder: 40,
              menuOrder: 15
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'general-ledger',
            name: '总账',
            icon: 'ep:collection-tag',
            component: 'erp/finance/general-ledger/index',
            componentName: 'ErpFinanceGeneralLedger',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/ledger-accounting',
              menuGroupTitle: '总账与核算',
              menuGroupIcon: 'ep:collection',
              menuGroupOrder: 40,
              menuOrder: 20
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'assets',
            name: '固定资产台账',
            icon: 'ep:coin',
            component: 'erp/finance/assets/index',
            componentName: 'ProjectFinanceAssets',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/ledger-accounting',
              menuGroupTitle: '总账与核算',
              menuGroupIcon: 'ep:collection',
              menuGroupOrder: 40,
              menuOrder: 20
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'reports',
            name: '财务报表',
            icon: 'ep:data-analysis',
            component: 'erp/finance/reports/index',
            componentName: 'ErpFinanceReports',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/report-analysis',
              menuGroupTitle: '报表与分析',
              menuGroupIcon: 'ep:data-analysis',
              menuGroupOrder: 50,
              menuOrder: 10
            },
            visible: true,
            keepAlive: false
          },
          {
            path: 'cost',
            name: '项目成本分析',
            icon: 'ep:pie-chart',
            component: 'erp/finance/cost/index',
            componentName: 'ProjectFinanceCost',
            redirect: '',
            meta: {
              menuGroupKey: '/finance/__group__/report-analysis',
              menuGroupTitle: '报表与分析',
              menuGroupIcon: 'ep:data-analysis',
              menuGroupOrder: 50,
              menuOrder: 20
            },
            visible: true,
            keepAlive: false
          }
        ]
      }
    ]
  },
  {
    path: '/sales',
    icon: 'ep:sell',
    name: '销售管理',
    component: '',
    componentName: 'ProjectSalesRoot',
    redirect: '/sales/contract',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    children: [
      {
        path: 'closure-workbench',
        name: '销售闭环工作台',
        icon: 'ep:data-analysis',
        component: 'erp/sale/order/closure-workbench',
        componentName: 'ProjectSalesClosureWorkbench',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  },
  {
    path: '/scm',
    icon: 'ep:management',
    name: '\u4f9b\u5e94\u94fe\u7ba1\u7406',
    component: '',
    componentName: 'ProjectScmRoot',
    redirect: '/scm/suggest',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    children: [
      {
        path: 'warehouse',
        name: '\u4ed3\u5e93\u4fe1\u606f',
        icon: 'ep:house',
        component: 'erp/stock/warehouse/index',
        componentName: 'ProjectScmWarehouse',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'stock',
        name: '\u4ea7\u54c1\u5e93\u5b58',
        icon: 'ep:coffee',
        component: 'erp/stock/stock/index',
        componentName: 'ProjectScmStock',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'stock-record',
        name: '\u5e93\u5b58\u660e\u7ec6',
        icon: 'fa-solid:blog',
        component: 'erp/stock/record/index',
        componentName: 'ProjectScmStockRecord',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'stock-analysis',
        name: '库存分析',
        icon: 'ep:data-analysis',
        component: 'erp/stock/analysis/index',
        componentName: 'ProjectScmStockAnalysis',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'stock-in',
        name: '\u5176\u4ed6\u5165\u5e93',
        icon: 'ep:zoom-in',
        component: 'erp/stock/in/index',
        componentName: 'ProjectScmStockIn',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'stock-out',
        name: '\u5176\u4ed6\u51fa\u5e93',
        icon: 'ep:zoom-out',
        component: 'erp/stock/out/index',
        componentName: 'ProjectScmStockOut',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'stock-move',
        name: '\u5e93\u5b58\u8c03\u62e8',
        icon: 'ep:folder-remove',
        component: 'erp/stock/move/index',
        componentName: 'ProjectScmStockMove',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'stock-check',
        name: '\u5e93\u5b58\u76d8\u70b9',
        icon: 'ep:circle-check-filled',
        component: 'erp/stock/check/index',
        componentName: 'ProjectScmStockCheck',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'purchase-request',
        name: '\u91c7\u8d2d\u9700\u6c42',
        icon: 'ep:tickets',
        component: 'erp/mrp/suggest/index',
        componentName: 'ProjectScmPurchaseRequest',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'purchase-order',
        name: '\u91c7\u8d2d\u8ba2\u5355\u53f0\u8d26',
        icon: 'ep:shopping-trolley',
        component: 'erp/purchase/order/index',
        componentName: 'ProjectScmPurchaseOrder',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'inbound',
        name: '\u6536\u8d27\u5165\u5e93',
        icon: 'ep:box',
        component: 'erp/purchase/in/index',
        componentName: 'ProjectScmInbound',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'outsource-inbound',
        name: '委外入库',
        icon: 'ep:box',
        component: 'erp/mrp/outsource-inbound/index',
        componentName: 'ProjectScmOutsourceInbound',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'production-inbound',
        name: '自制入库',
        icon: 'ep:box',
        component: 'erp/mrp/production-inbound/index',
        componentName: 'ProjectScmProductionInbound',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'return',
        name: '\u91c7\u8d2d\u9000\u8d27',
        icon: 'ep:minus',
        component: 'erp/purchase/return/index',
        componentName: 'ProjectScmPurchaseReturn',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'assemble',
        name: '\u7ec4\u88c5\u4e0e\u62c6\u5378',
        icon: 'ep:set-up',
        component: 'erp/stock/assemble/index',
        componentName: 'ProjectScmAssemble',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'netting-policy',
        name: '\u51c0\u9700\u6c42\u7b56\u7565',
        icon: 'ep:operation',
        component: 'erp/mrp/netting-policy/index',
        componentName: 'ProjectScmNettingPolicy',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  }
]

const shouldOmitLegacyMenu = (route: AppCustomRouteRecordRaw, parentChain: string[]) => {
  const currentSegment = normalizeRouteSegment(route.path)
  const currentChain = currentSegment ? [...parentChain, currentSegment].join('/') : parentChain.join('/')
  const currentComponent = normalizeComponentPath(route.component)

  return (
    duplicatedLegacyMenuComponents.has(currentComponent) ||
    duplicatedLegacyMenuChains.has(currentChain)
  )
}

const stripDuplicatedLegacyMenus = (
  routes: AppCustomRouteRecordRaw[] = [],
  parentChain: string[] = []
): AppCustomRouteRecordRaw[] => {
  const filteredRoutes: AppCustomRouteRecordRaw[] = []

  for (const route of routes) {
    if (shouldOmitLegacyMenu(route, parentChain)) {
      continue
    }

    const clonedRoute = cloneRouteTree(route)
    const currentSegment = normalizeRouteSegment(clonedRoute.path)
    const nextChain = currentSegment ? [...parentChain, currentSegment] : parentChain
    clonedRoute.children = stripDuplicatedLegacyMenus(clonedRoute.children || [], nextChain)
    filteredRoutes.push(clonedRoute)
  }

  return filteredRoutes
}

const markLegacyPromotedRoute = (
  route: AppCustomRouteRecordRaw,
  source: string
): AppCustomRouteRecordRaw => {
  route.meta = {
    ...(route.meta || {}),
    [legacyMenuSourceMetaKey]: source
  }
  return route
}

const promoteToTopLevel = (route: AppCustomRouteRecordRaw): AppCustomRouteRecordRaw => {
  const promoted = cloneRouteTree(route)
  const childPath = normalizeRoutePath(promoted.path)
  promoted.path = childPath.startsWith('/') ? childPath : `/${childPath}`
  promoted.parentId = 0
  return promoted
}

const flattenErpRootMenus = (
  remoteMenus: AppCustomRouteRecordRaw[] = []
): AppCustomRouteRecordRaw[] => {
  const flattenedMenus: AppCustomRouteRecordRaw[] = []

  for (const route of remoteMenus) {
    if (normalizeRoutePath(route.path) !== '/erp') {
      const clonedRoute = cloneRouteTree(route)
      clonedRoute.children = stripDuplicatedLegacyMenus(clonedRoute.children || [], [
        normalizeRouteSegment(clonedRoute.path)
      ])
      flattenedMenus.push(clonedRoute)
      continue
    }

    for (const child of route.children || []) {
      const childPath = normalizeRoutePath(child.path)
      if (childPath === 'home' || childPath === '/home') {
        continue
      }
      const promotedChild = markLegacyPromotedRoute(promoteToTopLevel(child), legacyMenuSourceErp)
      promotedChild.children = stripDuplicatedLegacyMenus(promotedChild.children || [], [
        normalizeRouteSegment(child.path)
      ])
      flattenedMenus.push(promotedChild)
    }
  }

  return flattenedMenus
}

const mergeRouteTree = (
  remoteRoute: AppCustomRouteRecordRaw,
  localRoute: AppCustomRouteRecordRaw
): AppCustomRouteRecordRaw => {
  const mergedRoute = cloneRouteTree(remoteRoute)
  mergedRoute.icon = mergedRoute.icon || localRoute.icon
  mergedRoute.name = mergedRoute.name || localRoute.name
  mergedRoute.component = resolveMergedComponent(mergedRoute.component, localRoute.component)
  mergedRoute.componentName = mergedRoute.componentName || localRoute.componentName
  mergedRoute.redirect = mergedRoute.redirect || localRoute.redirect
  mergedRoute.visible = mergedRoute.visible ?? localRoute.visible ?? true
  mergedRoute.keepAlive = mergedRoute.keepAlive ?? localRoute.keepAlive
  mergedRoute.alwaysShow = mergedRoute.alwaysShow ?? localRoute.alwaysShow
  mergedRoute.parentId = mergedRoute.parentId ?? localRoute.parentId
  mergedRoute.meta = {
    ...(localRoute.meta || {}),
    ...(mergedRoute.meta || {})
  }

  if (localRoute.children?.length) {
    const remoteChildren = mergedRoute.children || []
    const consumedChildren = new Set<number>()
    const mergedChildren: AppCustomRouteRecordRaw[] = []

    for (const child of localRoute.children) {
      const childIndex = remoteChildren.findIndex(
        (item) => normalizeRoutePath(item.path) === normalizeRoutePath(child.path)
      )
      if (childIndex === -1) {
        mergedChildren.push(cloneRouteTree(child))
        continue
      }
      consumedChildren.add(childIndex)
      mergedChildren.push(mergeRouteTree(remoteChildren[childIndex], child))
    }

    remoteChildren.forEach((child, index) => {
      if (!consumedChildren.has(index)) {
        mergedChildren.push(child)
      }
    })

    mergedRoute.children = mergedChildren
  }

  return mergedRoute
}

const mergeMenusByPath = (
  routes: AppCustomRouteRecordRaw[] = []
): AppCustomRouteRecordRaw[] => {
  const mergedRouteMap = new Map<string, AppCustomRouteRecordRaw>()
  const orderedPaths: string[] = []

  for (const route of routes) {
    const normalizedPath = normalizeRoutePath(route.path)
    if (!mergedRouteMap.has(normalizedPath)) {
      mergedRouteMap.set(normalizedPath, cloneRouteTree(route))
      orderedPaths.push(normalizedPath)
      continue
    }

    const existingRoute = mergedRouteMap.get(normalizedPath)!
    mergedRouteMap.set(normalizedPath, mergeRouteTree(existingRoute, route))
  }

  return orderedPaths
    .map((path) => mergedRouteMap.get(path))
    .filter((route): route is AppCustomRouteRecordRaw => Boolean(route))
}

const hasFormalMenuRoots = (remoteMenus: AppCustomRouteRecordRaw[] = []) => {
  return remoteMenus.some((route) => formalRootPaths.has(normalizeRoutePath(route.path)))
}

const shouldRetainRouteWhenFormalized = (route: AppCustomRouteRecordRaw) => {
  const normalizedPath = normalizeRoutePath(route.path)
  if (absorbedLegacyRootPaths.has(normalizedPath)) {
    return false
  }

  const legacySource = route.meta?.[legacyMenuSourceMetaKey]
  if (legacySource === legacyMenuSourceErp && !formalRootPaths.has(normalizedPath)) {
    return false
  }

  return true
}

export const mergeProjectDrivenMenus = (
  remoteMenus: AppCustomRouteRecordRaw[] = []
): AppCustomRouteRecordRaw[] => {
  if (!remoteMenus.length) {
    return []
  }

  const formalized = hasFormalMenuRoots(remoteMenus)
  const flattenedMenus = flattenErpRootMenus(remoteMenus)
  const normalizedMenus = formalized
    ? mergeMenusByPath(flattenedMenus).filter((route) => shouldRetainRouteWhenFormalized(route))
    : flattenedMenus
  const menuAugmentations = formalized
    ? mergeMenusByPath(flattenErpRootMenus(flatMenuAugmentations)).filter((route) =>
        shouldRetainRouteWhenFormalized(route)
      )
    : flatMenuAugmentations

  return decorateScmMenus(mergeMenusByPath([...normalizedMenus, ...menuAugmentations]))
}
