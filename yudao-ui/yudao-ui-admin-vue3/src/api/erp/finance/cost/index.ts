import request from '@/config/axios'

export interface FinanceCostEntryVO {
  id?: number
  productionOrderId?: number
  productionOrderNo?: string
  productId?: number
  productName?: string
  projectId?: number
  projectNo?: string
  projectName?: string
  costType?: number
  costTypeName?: string
  sourceType?: number
  sourceTypeName?: string
  accountingMonth?: string
  amount?: number | string
  remark?: string
  creatorName?: string
  createTime?: string
}

export interface FinanceCostSummaryVO {
  productionOrderId?: number
  productionOrderNo?: string
  productId?: number
  productName?: string
  projectId?: number
  projectNo?: string
  projectName?: string
  accountingMonth?: string
  totalAmount?: number | string
  breakdownCount?: number
  breakdown?: FinanceCostEntryVO[]
}

export interface FinanceCostPageReqVO {
  pageNo?: number
  pageSize?: number
  productionOrderId?: number
  costType?: number
  accountingMonth?: string
}

export interface FinanceCostDetailRespVO {
  productionOrderId?: number
  productionOrderNo?: string
  productId?: number
  productName?: string
  projectId?: number
  projectNo?: string
  projectName?: string
  finishedQty?: number | string
  materialCost?: number | string
  laborCost?: number | string
  depreciationCost?: number | string
  powerCost?: number | string
  otherCost?: number | string
  totalCost?: number | string
  unitCost?: number | string
  costSnapshotTime?: string
  materialDetails?: Array<{
    issueId?: number
    issueNo?: string
    issueTime?: string
    issueAmount?: number | string
    remark?: string
  }>
  costEntries?: Array<{
    id?: number
    costType?: number
    costTypeName?: string
    sourceType?: number
    sourceTypeName?: string
    sourceAllocationResultId?: number
    sourceAllocationId?: number
    sourceAllocationNo?: string
    accountingMonth?: string
    amount?: number | string
    remark?: string
    createTime?: string
  }>
}

export interface ErpProductionCostEntrySaveReqVO {
  id?: number
  productionOrderId?: number
  costType?: number
  sourceType?: number
  accountingMonth?: string
  amount?: number | string
  remark?: string
}

export interface FinanceCostProjectSummaryRespVO {
  projectId?: number
  projectNo?: string
  projectName?: string
  productionOrderCount?: number
  productCount?: number
  totalManHour?: number | string
  laborCost?: number | string
  depreciationCost?: number | string
  powerCost?: number | string
  otherCost?: number | string
  totalCost?: number | string
}

export const FinanceCostApi = {
  getPage: async (params?: FinanceCostPageReqVO) => {
    return await request.get<{ list: FinanceCostSummaryVO[]; total: number }>({
      url: '/erp/production-cost-entry/page',
      params
    })
  },

  update: async (data: ErpProductionCostEntrySaveReqVO) => {
    return await request.put({ url: '/erp/production-cost-entry/update', data })
  },

  getDetail: async (productionOrderId: number) => {
    return await request.get<FinanceCostDetailRespVO>({
      url: '/erp/production-cost-entry/detail',
      params: { productionOrderId }
    })
  },

  getProjectSummary: async (accountingMonth: string) => {
    return await request.get<FinanceCostProjectSummaryRespVO[]>({
      url: '/erp/production-cost-entry/project-summary',
      params: { accountingMonth }
    })
  },

  exportExcel: async (params?: FinanceCostPageReqVO) => {
    return await request.download({ url: '/erp/production-cost-entry/export-excel', params })
  },

  exportProjectSummaryExcel: async (accountingMonth: string) => {
    return await request.download({
      url: '/erp/production-cost-entry/export-project-summary-excel',
      params: { accountingMonth }
    })
  }
}
