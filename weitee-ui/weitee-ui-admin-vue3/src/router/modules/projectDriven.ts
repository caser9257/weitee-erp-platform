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

const mergeRouteTree = (
  remoteRoute: AppCustomRouteRecordRaw,
  localRoute: AppCustomRouteRecordRaw
): AppCustomRouteRecordRaw => {
  const mergedRoute = cloneRouteTree(remoteRoute)

  mergedRoute.icon = mergedRoute.icon || localRoute.icon
  mergedRoute.name = mergedRoute.name || localRoute.name
  mergedRoute.component = mergedRoute.component || localRoute.component
  mergedRoute.componentName = mergedRoute.componentName || localRoute.componentName
  mergedRoute.redirect = mergedRoute.redirect || localRoute.redirect
  // 本地项目菜单是可见性基准，避免后端残留的 hidden/visible=false 把注入菜单吞掉
  mergedRoute.visible = localRoute.visible ?? mergedRoute.visible ?? true
  mergedRoute.keepAlive = mergedRoute.keepAlive ?? localRoute.keepAlive
  mergedRoute.alwaysShow = mergedRoute.alwaysShow ?? localRoute.alwaysShow
  mergedRoute.parentId = mergedRoute.parentId ?? localRoute.parentId
  mergedRoute.meta = {
    ...(localRoute.meta || {}),
    ...(mergedRoute.meta || {})
  }

  if (!localRoute.children?.length) {
    return mergedRoute
  }

  const remoteChildren = mergedRoute.children || []
  const consumedChildren = new Set<number>()
  const orderedChildren: AppCustomRouteRecordRaw[] = []

  for (const child of localRoute.children) {
    const childIndex = remoteChildren.findIndex(
      (item) => normalizeRoutePath(item.path) === normalizeRoutePath(child.path)
    )
    if (childIndex === -1) {
      orderedChildren.push(cloneRouteTree(child))
      continue
    }
    consumedChildren.add(childIndex)
    orderedChildren.push(mergeRouteTree(remoteChildren[childIndex], child))
  }

  remoteChildren.forEach((child, index) => {
    if (!consumedChildren.has(index)) {
      orderedChildren.push(child)
    }
  })

  mergedRoute.children = orderedChildren
  return mergedRoute
}

const projectDrivenMenus: AppCustomRouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: '\u9996\u9875\u5de5\u4f5c\u53f0',
    icon: 'ep:monitor',
    component: 'dashboard/index',
    componentName: 'ProjectDashboard',
    redirect: '',
    meta: {},
    visible: true,
    keepAlive: false,
    parentId: 0
  },
  {
    path: '/pmo',
    name: '\u7efc\u5408\u8ba1\u5212\u7ba1\u7406',
    icon: 'ep:data-board',
    component: '',
    componentName: 'ProjectPmoRoot',
    redirect: '/pmo/project/master-plan',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    parentId: 0,
    children: [
      {
        path: 'project',
        name: '项目管理',
        icon: 'ep:calendar',
        component: '',
        componentName: 'ProjectPmoProjectGroup',
        redirect: '/pmo/project/master-plan',
        meta: {},
        visible: true,
        keepAlive: false,
        alwaysShow: true,
        children: [
          {
            path: 'master-plan',
            name: '项目主计划',
            icon: 'ep:calendar',
            component: 'pmo/project/index',
            componentName: 'ProjectPmoMasterPlan',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'follow-up',
            name: '项目跟进',
            icon: 'ep:checked',
            component: 'pmo/project/follow-up/index',
            componentName: 'ProjectPmoFollowUp',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'warning',
            name: '项目预警',
            icon: 'ep:warning',
            component: 'pmo/project/warning/index',
            componentName: 'ProjectPmoWarning',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          }
        ]
      },
      {
        path: 'organization',
        name: '组织与岗位',
        icon: 'fa:address-card',
        component: '',
        componentName: 'ProjectPmoOrganizationGroup',
        redirect: '/pmo/organization/org-chart',
        meta: {},
        visible: true,
        keepAlive: false,
        alwaysShow: true,
        children: [
          {
            path: 'org-chart',
            name: '组织架构',
            icon: 'ep:share',
            component: 'pmo/org-chart/index',
            componentName: 'ProjectPmoOrgChart',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'post-level-overview',
            name: '岗位层级',
            icon: 'ep:histogram',
            component: 'system/post-level/overview/index',
            componentName: 'ProjectPmoPostLevelOverview',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          }
        ]
      },
      {
        path: 'process',
        name: '流程协同',
        icon: 'ep:connection',
        component: '',
        componentName: 'ProjectPmoProcessGroup',
        redirect: '/pmo/process/process-monitor',
        meta: {},
        visible: true,
        keepAlive: false,
        alwaysShow: true,
        children: [
          {
            path: 'process-monitor',
            name: '流程监控',
            icon: 'ep:monitor',
            component: 'bpm/processInstance/report/index',
            componentName: 'ProjectPmoProcessMonitor',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          }
        ]
      },
      {
        path: 'analysis',
        name: '经营分析',
        icon: 'ep:trend-charts',
        component: '',
        componentName: 'ProjectPmoAnalysisGroup',
        redirect: '/pmo/analysis/kpi',
        meta: {},
        visible: true,
        keepAlive: false,
        alwaysShow: true,
        children: [
          {
            path: 'kpi',
            name: '经营指标看板',
            icon: 'ep:trend-charts',
            component: 'pmo/kpi/index',
            componentName: 'ProjectPmoKpi',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          }
        ]
      }
    ]
  },
  {
    path: '/sales',
    name: '\u9500\u552e\u7ba1\u7406',
    icon: 'ep:sell',
    component: '',
    componentName: 'ProjectSalesRoot',
    redirect: '/sales/contract',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    parentId: 0,
    children: [
      {
        path: 'contract',
        name: '\u5ba2\u6237\u4e0e\u5408\u540c\u8bc4\u5ba1',
        icon: 'ep:document-checked',
        component: 'crm/contract/index',
        componentName: 'ProjectSalesContract',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'order',
        name: '\u9500\u552e\u8ba2\u5355\u53f0\u8d26',
        icon: 'ep:list',
        component: 'erp/sale/order/index',
        componentName: 'ProjectSalesOrder',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'closure-workbench',
        name: '\u9500\u552e\u95ed\u73af\u5de5\u4f5c\u53f0',
        icon: 'ep:data-analysis',
        component: 'erp/sale/order/closure-workbench',
        componentName: 'ProjectSalesClosureWorkbench',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'delivery',
        name: '\u53d1\u8d27\u901a\u77e5\u4e0e\u7b7e\u6536',
        icon: 'ep:van',
        component: 'erp/sale/out/index',
        componentName: 'ProjectSalesDelivery',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  },
  {
    path: '/plm',
    name: '\u7814\u53d1\u4e0e\u5de5\u827a\u7ba1\u7406',
    icon: 'ep:document',
    component: '',
    componentName: 'ProjectPlmRoot',
    redirect: '/plm/standard-bom',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    parentId: 0,
    children: [
      {
        path: 'standard-bom',
        name: '\u6807\u51c6 BOM',
        icon: 'ep:collection',
        component: 'erp/rd/bom/index',
        componentName: 'ProjectPlmStandardBom',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'document',
        name: '\u7814\u53d1\u6587\u6863\u4e0e\u56fe\u7eb8\u5e93',
        icon: 'ep:folder-opened',
        component: 'erp/rd/document/index',
        componentName: 'ProjectPlmDocument',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'routing',
        name: '\u5de5\u827a\u8def\u7ebf\u4e0e SOP',
        icon: 'ep:connection',
        component: 'erp/route/index',
        componentName: 'ProjectPlmRouting',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  },
  {
    path: '/scm',
    name: '\u4f9b\u5e94\u94fe\u7ba1\u7406',
    icon: 'ep:shopping-cart-full',
    component: '',
    componentName: 'ProjectScmRoot',
    redirect: '/scm/suggest',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    parentId: 0,
    children: [
      {
        path: 'suggest',
        name: '\u91c7\u8d2d\u9700\u6c42',
        icon: 'ep:histogram',
        component: 'erp/mrp/suggest/index',
        componentName: 'ProjectScmPurchaseDemand',
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
        path: 'netting-policy',
        name: '\u51c0\u9700\u6c42\u7b56\u7565',
        icon: 'ep:operation',
        component: 'erp/mrp/netting-policy/index',
        componentName: 'ProjectScmNettingPolicy',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'substitute-material',
        name: '\u66ff\u4ee3\u6599\u53f0\u8d26',
        icon: 'ep:connection',
        component: 'erp/mrp/substitute/index',
        componentName: 'ProjectScmSubstitute',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'manufacture-bom',
        name: '\u5236\u9020BOM',
        icon: 'ep:collection',
        component: 'erp/mrp/bom/index',
        componentName: 'ProjectScmManufactureBom',
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
        path: 'assemble',
        name: '\u7ec4\u88c5\u4e0e\u62c6\u5378',
        icon: 'ep:set-up',
        component: 'erp/stock/assemble/index',
        componentName: 'ProjectScmAssemble',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  },
  {
    path: '/mes',
    name: '\u5236\u9020\u6267\u884c\u7ba1\u7406',
    icon: 'ep:operation',
    component: '',
    componentName: 'ProjectMesRoot',
    redirect: '/mes/work-order',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    parentId: 0,
    children: [
      {
        path: 'work-order',
        name: '\u751f\u4ea7\u5de5\u5355\u5927\u5385',
        icon: 'ep:calendar',
        component: 'mes/work-order/index',
        componentName: 'ProjectMesWorkOrder',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'pda-receive',
        name: 'PDA \u626b\u7801\u9886\u9000\u6599',
        icon: 'ep:cellphone',
        component: 'mes/pda-execute/receive',
        componentName: 'ProjectMesPdaReceive',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'pda-report',
        name: 'PDA \u626b\u7801\u62a5\u5de5',
        icon: 'ep:iphone',
        component: 'mes/pda-execute/report',
        componentName: 'ProjectMesPdaReport',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'equipment',
        name: '\u8bbe\u5907\u53f0\u8d26',
        icon: 'ep:cpu',
        component: 'mes/equipment/index',
        componentName: 'ProjectMesEquipment',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  },
  {
    path: '/qms',
    name: '\u8d28\u91cf\u7ba1\u7406',
    icon: 'ep:medal',
    component: '',
    componentName: 'ProjectQmsRoot',
    redirect: '/qms/iqc',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    parentId: 0,
    children: [
      {
        path: 'iqc',
        name: '\u6765\u6599\u68c0\u9a8c',
        icon: 'ep:finished',
        component: 'qms/iqc/IqcEntry',
        componentName: 'ProjectQmsIqc',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'ipqc',
        name: '\u8fc7\u7a0b\u68c0\u9a8c',
        icon: 'ep:aim',
        component: 'qms/ipqc/index',
        componentName: 'ProjectQmsIpqc',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'oqc',
        name: '\u5b8c\u5de5\u4e0e\u51fa\u8d27\u68c0\u9a8c',
        icon: 'ep:circle-check',
        component: 'qms/oqc/index',
        componentName: 'ProjectQmsOqc',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  },
  {
    path: '/erp',
    name: 'ERP',
    icon: '',
    component: '',
    componentName: 'ProjectErpRoot',
    redirect: '',
    meta: {},
    visible: true,
    keepAlive: false,
    parentId: 0,
    children: [
      {
        path: 'finance',
        name: '\u8d22\u52a1\u7ba1\u7406',
        icon: 'ep:money',
        component: '',
        componentName: 'ProjectFinanceRoot',
        redirect: '/erp/finance/cost',
        meta: {},
        visible: true,
        keepAlive: false,
        alwaysShow: true,
        children: [
          {
            path: 'cost',
            name: '\u9879\u76ee\u6210\u672c\u5206\u6790',
            icon: 'ep:pie-chart',
            component: 'erp/finance/cost/index',
            componentName: 'ProjectFinanceCost',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'apar',
            name: '\u5e94\u6536\u5e94\u4ed8\u8d26\u6b3e\u6c60',
            icon: 'ep:wallet',
            component: 'erp/finance/apar/index',
            componentName: 'ProjectFinanceApar',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'ar-statement',
            name: '\u5e94\u6536\u53f0\u8d26',
            icon: 'ep:money',
            component: 'erp/finance/ar-statement/index',
            componentName: 'ErpArStatement',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'ap-estimate',
            name: '\u91c7\u8d2d\u6682\u4f30\u5165\u5e93',
            icon: 'ep:clock',
            component: 'erp/finance/ap-estimate/index',
            componentName: 'ProjectFinanceApEstimate',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'ledger',
            name: '\u8d22\u52a1\u8d26\u7c3f',
            icon: 'ep:collection',
            component: 'erp/finance/ledger/index',
            componentName: 'ErpFinanceLedger',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'period',
            name: '\u4f1a\u8ba1\u671f\u95f4',
            icon: 'ep:calendar',
            component: 'erp/finance/period/index',
            componentName: 'ErpFinancePeriod',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'subject',
            name: '\u8d22\u52a1\u79d1\u76ee',
            icon: 'ep:document',
            component: 'erp/finance/subject/index',
            componentName: 'ErpFinanceSubject',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'report-item',
            name: '\u62a5\u8868\u9879\u76ee',
            icon: 'ep:list',
            component: 'erp/finance/report-item/index',
            componentName: 'ErpFinanceReportItem',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'reports',
            name: '\u8d22\u52a1\u62a5\u8868',
            icon: 'ep:data-analysis',
            component: 'erp/finance/reports/index',
            componentName: 'ErpFinanceReports',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'voucher',
            name: '\u8d22\u52a1\u51ed\u8bc1',
            icon: 'ep:tickets',
            component: 'erp/finance/voucher/index',
            componentName: 'ErpFinanceVoucher',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'receipt',
            name: '\u9879\u76ee\u6536\u6b3e\u7ba1\u7406',
            icon: 'ep:expand',
            component: 'erp/finance/receipt/index',
            componentName: 'ErpFinanceReceipt',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'payment',
            name: '\u9879\u76ee\u4ed8\u6b3e\u7ba1\u7406',
            icon: 'ep:caret-right',
            component: 'erp/finance/payment/index',
            componentName: 'ErpFinancePayment',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'account',
            name: '\u7ed3\u7b97\u8d26\u6237',
            icon: 'fa:universal-access',
            component: 'erp/finance/account/index',
            componentName: 'ErpAccount',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          },
          {
            path: 'assets',
            name: '\u56fa\u5b9a\u8d44\u4ea7\u53f0\u8d26',
            icon: 'ep:coin',
            component: 'erp/finance/assets/index',
            componentName: 'ProjectFinanceAssets',
            redirect: '',
            meta: {},
            visible: true,
            keepAlive: false
          }
        ]
      }
    ]
  },
  {
    path: '/hr',
    name: '\u4eba\u4e8b\u7ba1\u7406',
    icon: 'ep:user',
    component: '',
    componentName: 'ProjectHrRoot',
    redirect: '/hr/archive',
    meta: {},
    visible: true,
    keepAlive: false,
    alwaysShow: true,
    parentId: 0,
    children: [
      {
        path: 'archive',
        name: '\u5458\u5de5\u6863\u6848',
        icon: 'ep:files',
        component: 'hr/archive/index',
        componentName: 'ProjectHrArchive',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      },
      {
        path: 'appraisal',
        name: '\u6708\u5ea6\u7ee9\u6548\u8003\u6838',
        icon: 'ep:data-analysis',
        component: 'hr/appraisal/index',
        componentName: 'ProjectHrAppraisal',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      }
    ]
  }
]

export const mergeProjectDrivenMenus = (
  remoteMenus: AppCustomRouteRecordRaw[] = []
): AppCustomRouteRecordRaw[] => {
  const mergedMenus = remoteMenus.map((route) => cloneRouteTree(route))
  const prependedMenus: AppCustomRouteRecordRaw[] = []

  for (const localRoute of projectDrivenMenus) {
    const routeIndex = mergedMenus.findIndex(
      (item) => normalizeRoutePath(item.path) === normalizeRoutePath(localRoute.path)
    )

    if (routeIndex === -1) {
      prependedMenus.push(cloneRouteTree(localRoute))
      continue
    }

    mergedMenus[routeIndex] = mergeRouteTree(mergedMenus[routeIndex], localRoute)
  }

  return [...prependedMenus, ...mergedMenus]
}
