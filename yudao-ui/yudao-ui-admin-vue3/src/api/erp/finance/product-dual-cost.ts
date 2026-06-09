import request from '@/config/axios'

export interface DualProductCostVO {
  id?: number
  productId?: number
  productNo?: string
  productName?: string
  productBatchNo?: string
  productionOrderId?: number
  productionOrderNo?: string
  period?: string
  externalMaterialAmount?: number
  internalMaterialAmount?: number
  externalLaborAmount?: number
  internalLaborAmount?: number
  externalOverheadAmount?: number
  internalOverheadAmount?: number
  externalTotalAmount?: number
  internalTotalAmount?: number
  diffAmount?: number
  status?: number
  versionNo?: number
  lastRebuildTime?: string
  remark?: string
  createTime?: string
}

export interface DualProductCostPageReqVO {
  pageNo?: number
  pageSize?: number
  productId?: number
  productNo?: string
  productName?: string
  productBatchNo?: string
  productionOrderId?: number
  period?: string
}

export interface DualProductCostRebuildReqVO {
  productId: number
  productionOrderId?: number
  productBatchNo?: string
  period: string
  remark?: string
}

export const DualProductCostApi = {
  getProductDualCostPage(params: DualProductCostPageReqVO) {
    return request.get({ url: '/erp/finance-dual-product-cost/page', params })
  },
  getProductDualCost(id: number) {
    return request.get({ url: '/erp/finance-dual-product-cost/get', params: { id } })
  },
  getProductDualCostItems(resultId: number) {
    return request.get({ url: '/erp/finance-dual-product-cost/items', params: { resultId } })
  },
  rebuildProductDualCost(data: DualProductCostRebuildReqVO) {
    return request.post({ url: '/erp/finance-dual-product-cost/rebuild', data })
  },
  exportExternalProductCost(params: DualProductCostPageReqVO) {
    return request.get({ url: '/erp/finance-dual-product-cost/export-external', params, responseType: 'blob' })
  },
  exportInternalProductCost(params: DualProductCostPageReqVO) {
    return request.get({ url: '/erp/finance-dual-product-cost/export-internal', params, responseType: 'blob' })
  }
}
