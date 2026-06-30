import request from '@/config/axios'

export interface FinanceCostEntryVO {
  id?: number
  productionOrderId?: number
  productionOrderNo?: string
  projectId?: number
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
  projectId?: number
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

export const FinanceCostApi = {
  getPage: async (params?: FinanceCostPageReqVO) => {
    return await request.get({ url: '/erp/production-cost-entry/page', params })
  },

  getDetail: async (productionOrderId: number) => {
    return await request.get<FinanceCostDetailRespVO>({
      url: '/erp/production-cost-entry/detail',
      params: { productionOrderId }
    })
  }
}
