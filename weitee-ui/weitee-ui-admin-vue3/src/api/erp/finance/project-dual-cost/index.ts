import request from '@/config/axios'

export interface DualProjectCostVO {
  id?: number
  projectNo?: string
  projectName?: string
  projectId?: number
  period?: string
  costType?: number
  costTypeName?: string
  externalAmount?: number
  internalAmount?: number
  diffAmount?: number
  sourceCount?: number
}

export interface DualProjectCostPageReqVO {
  pageNo?: number
  pageSize?: number
  projectId?: number
  projectNo?: string
  projectName?: string
  period?: string
  costType?: number
}

export const COST_TYPE_OPTIONS = [
  { label: '材料成本', value: 1 },
  { label: '人工成本', value: 2 },
  { label: '制造费用', value: 3 },
  { label: '外协成本', value: 4 }
]

export const DualProjectCostApi = {
  getProjectDualCostPage: async (params: DualProjectCostPageReqVO) => {
    return await request.get({ url: `/erp/finance-dual-project-cost/page`, params })
  },
  getProjectDualCostItems: async (id: number) => {
    return await request.get({ url: `/erp/finance-dual-project-cost/items?resultId=${id}` })
  },
  rebuildProjectDualCost: async (data: {
    projectId: number
    period: string
    remark: string
  }) => {
    return await request.post({ url: `/erp/finance-dual-project-cost/rebuild`, data })
  },
  exportExternalProjectCost: async (params: DualProjectCostPageReqVO) => {
    return await request.download({ url: `/erp/finance-dual-project-cost/export-external`, params })
  },
  exportInternalProjectCost: async (params: DualProjectCostPageReqVO) => {
    return await request.download({ url: `/erp/finance-dual-project-cost/export-internal`, params })
  }
}
