import { Layout } from '@/utils/routerHelper'

const { t } = useI18n()
/**
 * redirect: noredirect        当设置 noredirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'          设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * meta : {
 hidden: true              当设置 true 的时候该路由不会再侧边栏出现 如404，login等页面(默认 false)

 alwaysShow: true          当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式，
 只有一个时，会将那个子路由当做根路由显示在侧边栏，
 若你想不管路由下面的 children 声明的个数都显示你的根路由，
 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，
 一直显示根路由(默认 false)

 title: 'title'            设置该路由在侧边栏和面包屑中展示的名字

 icon: 'svg-name'          设置该路由的图标

 noCache: true             如果设置为true，则不会被 <keep-alive> 缓存(默认 false)

 breadcrumb: false         如果设置为false，则不会在breadcrumb面包屑中显示(默认 true)

 affix: true               如果设置为true，则会一直固定在tag项中(默认 false)

 noTagsView: true          如果设置为true，则不会出现在tag中(默认 false)

 activeMenu: '/dashboard'  显示高亮的路由路径

 followAuth: '/dashboard'  跟随哪个路由进行权限过滤

 canTo: true               设置为true即使hidden为true，也依然可以进行路由跳转(默认 false)
 }
 **/
const remainingRouter: AppRouteRecordRaw[] = [
  {
    path: '/redirect',
    component: Layout,
    name: 'Redirect',
    children: [
      {
        path: '/redirect/:path(.*)',
        name: 'Redirect',
        component: () => import('@/views/Redirect/Redirect.vue'),
        meta: {}
      }
    ],
    meta: {
      hidden: true,
      noTagsView: true
    }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    name: 'Home',
    meta: {},
    children: [
      {
        path: 'index',
        component: () => import('@/views/Home/WorkbenchIndustrial.vue'),
        name: 'Index',
        meta: {
          title: t('router.home'),
          icon: 'ep:home-filled',
          noCache: false,
          affix: true
        }
      },
      {
        path: 'mrp/finish-quality',
        component: () => import('@/views/erp/mrp/finish-quality/index.vue'),
        name: 'ErpProductionFinishQualityPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '成品质检',
          activeMenu: '/qms/finish-quality'
        }
      },
      {
        path: 'mrp/outsource-inbound',
        component: () => import('@/views/erp/mrp/outsource-inbound/index.vue'),
        name: 'ErpMrpOutsourceInboundPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '委外入库',
          activeMenu: '/scm/outsource-inbound'
        }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    name: 'UserInfo',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'profile',
        component: () => import('@/views/Profile/Index.vue'),
        name: 'Profile',
        meta: {
          canTo: true,
          hidden: true,
          noTagsView: false,
          icon: 'ep:user',
          title: t('common.profile')
        }
      },
      {
        path: 'notify-message',
        component: () => import('@/views/system/notify/my/index.vue'),
        name: 'MyNotifyMessage',
        meta: {
          canTo: true,
          hidden: true,
          noTagsView: false,
          icon: 'ep:message',
          title: '我的站内信'
        }
      }
    ]
  },
  {
    path: '/project',
    component: Layout,
    name: 'ProjectWarningRedirect',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'warning',
        component: () => import('@/views/pmo/project/warning/index.vue'),
        name: 'ProjectWarningPage',
        meta: {
          title: '项目预警',
          noCache: true,
          hidden: true,
          canTo: true,
          icon: 'ep:warning',
          activeMenu: '/project/project'
        }
      },
      {
        path: 'project/detail/:id',
        component: () => import('@/views/project/project/detail/index.vue'),
        name: 'ProjectDetail',
        meta: {
          title: '项目详情',
          noCache: true,
          hidden: true,
          canTo: true,
          icon: 'ep:document',
          activeMenu: '/project/project'
        }
      }
    ]
  },
  {
    path: '/dict',
    component: Layout,
    name: 'dict',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'type/data/:dictType',
        component: () => import('@/views/system/dict/data/index.vue'),
        name: 'SystemDictData',
        meta: {
          title: '字典数据',
          noCache: true,
          hidden: true,
          canTo: true,
          icon: '',
          activeMenu: '/system/dict'
        }
      }
    ]
  },

  {
    path: '/codegen',
    component: Layout,
    name: 'CodegenEdit',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'edit',
        component: () => import('@/views/infra/codegen/EditTable.vue'),
        name: 'InfraCodegenEditTable',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          icon: 'ep:edit',
          title: '修改生成配置',
          activeMenu: 'infra/codegen/index'
        }
      }
    ]
  },
  {
    path: '/job',
    component: Layout,
    name: 'JobL',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'job-log',
        component: () => import('@/views/infra/job/logger/index.vue'),
        name: 'InfraJobLog',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          icon: 'ep:edit',
          title: '调度日志',
          activeMenu: 'infra/job/index'
        }
      }
    ]
  },
  {
    path: '/erp',
    component: Layout,
    name: 'Erp',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'sale/order',
        component: () => import('@/views/erp/sale/order/index.vue'),
        name: 'ErpSaleOrderCompatPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '销售订单台账',
          activeMenu: '/sales/order'
        }
      },
      {
        path: 'sale/order/closure-workbench',
        component: () => import('@/views/erp/sale/order/closure-workbench.vue'),
        name: 'ErpSaleOrderClosureWorkbenchCompatPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '销售闭环工作台',
          activeMenu: '/sales/closure-workbench'
        }
      },
      {
        path: 'sale/order/restart',
        component: () => import('@/views/erp/sale/order/bpm/restart/index.vue'),
        name: 'ErpSaleOrderRestartPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '销售订单重新审批',
          activeMenu: '/approval/submitted'
        },
        props: (route) => ({
          id: route.query.id,
          from: route.query.from
        })
      },
      {
        path: 'sale/order/mrp-trace',
        component: () => import('@/views/erp/sale/order/mrp-trace.vue'),
        name: 'ErpSaleOrderMrpTracePage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '销售订单 MRP 追踪',
          activeMenu: '/sales/closure-workbench'
        }
      },
      {
        path: 'sale/order/purchase-trace',
        component: () => import('@/views/erp/purchase/order/index.vue'),
        name: 'ErpSaleOrderPurchaseOrderTracePage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '销售订单采购追踪',
          activeMenu: '/sales/order'
        }
      },
      {
        path: 'sale/return',
        component: () => import('@/views/erp/sale/return/index.vue'),
        name: 'ErpSaleReturnCompatPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '销售退货',
          activeMenu: '/sales/return'
        }
      },
      {
        path: 'sale/customer',
        component: () => import('@/views/erp/sale/customer/index.vue'),
        name: 'ErpSaleCustomerCompatPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '客户管理',
          activeMenu: '/sales/customer'
        }
      },
      {
        path: 'sale/shipment-release',
        component: () => import('@/views/erp/sale/shipment-release/index.vue'),
        name: 'ErpShipmentReleaseCompatPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '发货放行审核',
          activeMenu: '/sales/shipment-release'
        }
      },
      {
        path: 'sale/market-ledger',
        component: () => import('@/views/erp/sale/market-ledger/index.vue'),
        name: 'ErpMarketLedgerCompatPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '市场执行台账',
          activeMenu: '/sales/market-ledger'
        }
      },
      {
        path: 'sale/market-alert',
        component: () => import('@/views/erp/sale/market-alert/index.vue'),
        name: 'ErpMarketAlertCompatPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '市场预警与统计',
          activeMenu: '/sales/market-alert'
        }
      },
      {
        path: 'purchase/order',
        component: () => import('@/views/erp/purchase/order/index.vue'),
        name: 'ErpPurchaseOrderCompatPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '采购订单台账',
          activeMenu: '/scm/purchase-order'
        }
      },
      {
        path: 'purchase/order/bpm/detail',
        component: () => import('@/views/erp/purchase/order/bpm/detail/index.vue'),
        name: 'ErpPurchaseOrderBpmDetailPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '采购订单详情',
          activeMenu: '/scm/purchase-order'
        },
        props: (route) => ({
          id: route.query.id
        })
      },
      {
        path: 'purchase/in',
        component: () => import('@/views/erp/purchase/in/index.vue'),
        name: 'ErpPurchaseInCompatPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '收货入库',
          activeMenu: '/scm/inbound'
        }
      },
      {
        path: 'purchase/in/bpm/detail',
        component: () => import('@/views/erp/purchase/in/bpm/detail/index.vue'),
        name: 'ErpPurchaseInBpmDetailPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '采购入库质检详情',
          activeMenu: '/scm/inbound'
        },
        props: (route) => ({
          id: route.query.id
        })
      },
      {
        path: 'purchase/in-quality/detail',
        component: () => import('@/views/erp/purchase/in-quality/detail/index.vue'),
        name: 'ErpPurchaseInQualityDetailPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '采购入库质检单详情',
          activeMenu: '/qms/iqc'
        }
      },
      {
        path: '/qms/iqc/detail',
        component: () => import('@/views/erp/purchase/in-quality/detail/index.vue'),
        name: 'QmsIqcDetailPage',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '来料检验单详情',
          activeMenu: '/qms/iqc'
        }
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/Login/Login.vue'),
    name: 'Login',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/sso',
    component: () => import('@/views/Login/Login.vue'),
    name: 'SSOLogin',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/social-login',
    component: () => import('@/views/Login/SocialLogin.vue'),
    name: 'SocialLogin',
    meta: {
      hidden: true,
      title: t('router.socialLogin'),
      noTagsView: true
    }
  },
  {
    path: '/403',
    component: () => import('@/views/Error/403.vue'),
    name: 'NoAccess',
    meta: {
      hidden: true,
      title: '403',
      noTagsView: true
    }
  },
  {
    path: '/404',
    component: () => import('@/views/Error/404.vue'),
    name: 'NoFound',
    meta: {
      hidden: true,
      title: '404',
      noTagsView: true
    }
  },
  {
    path: '/500',
    component: () => import('@/views/Error/500.vue'),
    name: 'Error',
    meta: {
      hidden: true,
      title: '500',
      noTagsView: true
    }
  },
  {
    path: '/approval',
    component: Layout,
    name: 'ApprovalCenterRemaining',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'todo',
        component: () => import('@/views/bpm/approval/portal/index.vue'),
        name: 'ApprovalPortalTodoRemaining',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '待我审批',
          activeMenu: '/approval/todo'
        }
      },
      {
        path: 'submitted',
        component: () => import('@/views/bpm/approval/portal/index.vue'),
        name: 'ApprovalPortalSubmittedRemaining',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '我发起的',
          activeMenu: '/approval/submitted'
        }
      },
      {
        path: 'done',
        component: () => import('@/views/bpm/approval/portal/index.vue'),
        name: 'ApprovalPortalDoneRemaining',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '已审批',
          activeMenu: '/approval/done'
        }
      },
      {
        path: 'cc',
        component: () => import('@/views/bpm/approval/portal/index.vue'),
        name: 'ApprovalPortalCcRemaining',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '抄送我',
          activeMenu: '/approval/cc'
        }
      }
    ]
  },
  {
    path: '/bpm',
    component: Layout,
    name: 'bpm',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'manager/form/edit',
        component: () => import('@/views/bpm/form/editor/index.vue'),
        name: 'BpmFormEditor',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '设计流程表单',
          activeMenu: '/bpm/manager/form'
        }
      },
      {
        path: 'manager/definition',
        component: () => import('@/views/bpm/model/definition/index.vue'),
        name: 'BpmProcessDefinition',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '流程定义',
          activeMenu: '/bpm/manager/model'
        }
      },
      {
        path: 'process-instance/create',
        component: () => import('@/views/bpm/processInstance/create/index.vue'),
        name: 'BpmProcessInstanceCreate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '发起流程',
          activeMenu: '/approval/submitted'
        }
      },
      {
        path: 'process-instance/detail',
        component: () => import('@/views/bpm/processInstance/detail/index.vue'),
        name: 'BpmProcessInstanceDetail',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '流程详情',
          activeMenu: '/approval/submitted'
        },
        props: (route) => ({
          id: route.query.id,
          taskId: route.query.taskId,
          activityId: route.query.activityId
        })
      },
      {
        path: 'process-instance/report',
        component: () => import('@/views/bpm/processInstance/report/index.vue'),
        name: 'BpmProcessInstanceReport',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '数据报表',
          activeMenu: '/bpm/manager/model'
        }
      },
      {
        path: 'oa/leave/create',
        component: () => import('@/views/bpm/oa/leave/create.vue'),
        name: 'OALeaveCreate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '发起 OA 请假',
          activeMenu: '/bpm/oa/leave'
        }
      },
      {
        path: 'oa/leave/detail',
        component: () => import('@/views/bpm/oa/leave/detail.vue'),
        name: 'OALeaveDetail',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '查看 OA 请假',
          activeMenu: '/bpm/oa/leave'
        }
      },
      {
        path: 'manager/model/create',
        component: () => import('@/views/bpm/model/form/index.vue'),
        name: 'BpmModelCreate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '创建流程',
          activeMenu: '/bpm/manager/model'
        }
      },
      {
        path: 'manager/model/:type/:id',
        component: () => import('@/views/bpm/model/form/index.vue'),
        name: 'BpmModelUpdate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '修改流程',
          activeMenu: '/bpm/manager/model'
        }
      }
    ]
  },
  {
    path: '/pay',
    component: Layout,
    name: 'pay',
    meta: { hidden: true },
    children: [
      {
        path: 'cashier',
        name: 'PayCashier',
        meta: {
          title: '收银台',
          noCache: true,
          hidden: true
        },
        component: () => import('@/views/pay/cashier/index.vue')
      }
    ]
  },
  {
    path: '/crm',
    component: Layout,
    name: 'CrmCenter',
    meta: { hidden: true },
    children: [
      {
        path: 'clue/detail/:id',
        name: 'CrmClueDetail',
        meta: {
          title: '线索详情',
          noCache: true,
          hidden: true,
          activeMenu: '/crm/clue'
        },
        component: () => import('@/views/crm/clue/detail/index.vue')
      },
      {
        path: 'customer/detail/:id',
        name: 'CrmCustomerDetail',
        meta: {
          title: '客户详情',
          noCache: true,
          hidden: true,
          activeMenu: '/crm/customer'
        },
        component: () => import('@/views/crm/customer/detail/index.vue')
      },
      {
        path: 'business/detail/:id',
        name: 'CrmBusinessDetail',
        meta: {
          title: '商机详情',
          noCache: true,
          hidden: true,
          activeMenu: '/crm/business'
        },
        component: () => import('@/views/crm/business/detail/index.vue')
      },
      {
        path: 'contract/detail/:id',
        name: 'CrmContractDetail',
        meta: {
          title: '合同详情',
          noCache: true,
          hidden: true,
          activeMenu: '/crm/contract'
        },
        component: () => import('@/views/crm/contract/detail/index.vue')
      },
      {
        path: 'receivable-plan/detail/:id',
        name: 'CrmReceivablePlanDetail',
        meta: {
          title: '回款计划详情',
          noCache: true,
          hidden: true,
          activeMenu: '/crm/receivable-plan'
        },
        component: () => import('@/views/crm/receivable/plan/detail/index.vue')
      },
      {
        path: 'receivable/detail/:id',
        name: 'CrmReceivableDetail',
        meta: {
          title: '回款详情',
          noCache: true,
          hidden: true,
          activeMenu: '/crm/receivable'
        },
        component: () => import('@/views/crm/receivable/detail/index.vue')
      },
      {
        path: 'contact/detail/:id',
        name: 'CrmContactDetail',
        meta: {
          title: '联系人详情',
          noCache: true,
          hidden: true,
          activeMenu: '/crm/contact'
        },
        component: () => import('@/views/crm/contact/detail/index.vue')
      },
      {
        path: 'product/detail/:id',
        name: 'CrmProductDetail',
        meta: {
          title: '产品详情',
          noCache: true,
          hidden: true,
          activeMenu: '/crm/product'
        },
        component: () => import('@/views/crm/product/detail/index.vue')
      }
    ]
  },
  {
    path: '/ai',
    component: Layout,
    name: 'Ai',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'image/square',
        component: () => import('@/views/ai/image/square/index.vue'),
        name: 'AiImageSquare',
        meta: {
          title: '绘图作品',
          icon: 'ep:home-filled',
          noCache: false
        }
      },
      {
        path: 'knowledge/document',
        component: () => import('@/views/ai/knowledge/document/index.vue'),
        name: 'AiKnowledgeDocument',
        meta: {
          title: '知识库文档',
          icon: 'ep:document',
          noCache: false,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'knowledge/document/create',
        component: () => import('@/views/ai/knowledge/document/form/index.vue'),
        name: 'AiKnowledgeDocumentCreate',
        meta: {
          title: '创建文档',
          icon: 'ep:plus',
          noCache: true,
          hidden: true,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'knowledge/document/update',
        component: () => import('@/views/ai/knowledge/document/form/index.vue'),
        name: 'AiKnowledgeDocumentUpdate',
        meta: {
          title: '修改文档',
          icon: 'ep:edit',
          noCache: true,
          hidden: true,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'knowledge/retrieval',
        component: () => import('@/views/ai/knowledge/knowledge/retrieval/index.vue'),
        name: 'AiKnowledgeRetrieval',
        meta: {
          title: '文档召回测试',
          icon: 'ep:search',
          noCache: true,
          hidden: true,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'knowledge/segment',
        component: () => import('@/views/ai/knowledge/segment/index.vue'),
        name: 'AiKnowledgeSegment',
        meta: {
          title: '知识库分段',
          icon: 'ep:tickets',
          noCache: true,
          hidden: true,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'console/workflow/create',
        component: () => import('@/views/ai/workflow/form/index.vue'),
        name: 'AiWorkflowCreate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '设计 AI 工作流',
          activeMenu: '/ai/console/workflow'
        }
      },
      {
        path: 'console/workflow/:type/:id',
        component: () => import('@/views/ai/workflow/form/index.vue'),
        name: 'AiWorkflowUpdate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '设计 AI 工作流',
          activeMenu: '/ai/console/workflow'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/Error/404.vue'),
    name: '',
    meta: {
      title: '404',
      hidden: true,
      breadcrumb: false
    }
  },
  {
    path: '/iot',
    component: Layout,
    name: 'IOT',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'product/product/detail/:id',
        name: 'IoTProductDetail',
        meta: {
          title: '产品详情',
          noCache: true,
          hidden: true,
          activeMenu: '/iot/device/product'
        },
        component: () => import('@/views/iot/product/product/detail/index.vue')
      },
      {
        path: 'device/detail/:id',
        name: 'IoTDeviceDetail',
        meta: {
          title: '设备详情',
          noCache: true,
          hidden: true,
          activeMenu: '/iot/device/device'
        },
        component: () => import('@/views/iot/device/device/detail/index.vue')
      },
      {
        path: 'ota/operation/firmware/detail/:id',
        name: 'IoTOtaFirmwareDetail',
        meta: {
          title: '固件详情',
          noCache: true,
          hidden: true,
          activeMenu: '/iot/operation/ota/firmware'
        },
        component: () => import('@/views/iot/ota/firmware/detail/index.vue')
      }
    ]
  },
  {
    path: '/rd/rd-bom',
    component: Layout,
    name: 'RdBomCompatPage',
    meta: {
      title: '研发 BOM',
      hidden: true,
      noTagsView: true,
      activeMenu: '/plm/standard-bom'
    },
    children: [
      {
        path: '',
        name: 'RdBomCompatIndex',
        component: () => import('@/views/erp/rd/rd-bom/index.vue'),
        meta: {}
      }
    ]
  }
]

export default remainingRouter
