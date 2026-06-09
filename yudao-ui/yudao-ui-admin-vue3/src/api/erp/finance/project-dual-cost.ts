import request from '@/config/axios'

// ========== 类型定义 ==========

/** 项目双账成本 Response VO */
export interface DualProjectCostVO {
  id?: number
  projectId?: number
  projectNo?: string
  projectName?: string
  period?: string
  costType?: number
  costTypeName?: string
  externalAmount?: number
  internalAmount?: number
  diffAmount?: number
  sourceCount?: number
  status?: number
  versionNo?: number
  lastRebuildTime?: string
  lastRebuildBy?: number
  remark?: string
  createTime?: string
}

/** 项目双账成本分页查询 Request VO */
export interface DualProjectCostPageReqVO {
  pageNo?: number
  pageSize?: number
  projectId?: number
  projectNo?: string
  projectName?: string
  period?: string
  costType?: number
}

/** 项目双账成本重跑 Request VO */
export interface DualProjectCostRebuildReqVO {
  projectId: number
  period: string
  remark?: string
}

// ========== 成本类别常量 ==========

export const COST_TYPE_OPTIONS = [
  { label: '材料', value: 10 },
  { label: '人工', value: 20 },
  { label: '折旧', value: 30 },
  { label: '电费', value: 40 },
  { label: '其他', value: 50 }
]

export const getCostTypeLabel = (value?: number) =>
  COST_TYPE_OPTIONS.find(item => item.value === value)?.label || '-'

// ========== API ==========

export const DualProjectCostApi = {
  /** 获得项目双账成本分页 */
  getProjectDualCostPage(params: DualProjectCostPageReqVO) {
    return request.get({ url: '/erp/finance-dual-project-cost/page', params })
  },

  /** 获得单个项目双账成本 */
  getProjectDualCost(id: number) {
    return request.get({ url: '/erp/finance-dual-project-cost/get', params: { id } })
  },

  /** 获得项目双账成本明细 */
  getProjectDualCostItems(resultId: number) {
    return request.get({ url: '/erp/finance-dual-project-cost/items', params: { resultId } })
  },

  /** 项目级重跑 */
  rebuildProjectDualCost(data: DualProjectCostRebuildReqVO) {
    return request.post({ url: '/erp/finance-dual-project-cost/rebuild', data })
  },

  /** 导出外部账项目成本 */
  exportExternalProjectCost(params: DualProjectCostPageReqVO) {
    return request.get({ url: '/erp/finance-dual-project-cost/export-external', params, responseType: 'blob' })
  },

  /** 导出内部账项目成本 */
  exportInternalProjectCost(params: DualProjectCostPageReqVO) {
    return request.get({ url: '/erp/finance-dual-project-cost/export-internal', params, responseType: 'blob' })
  }
}
