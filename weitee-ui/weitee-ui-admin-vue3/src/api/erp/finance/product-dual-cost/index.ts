import request from '@/config/axios'

export interface DualProductCostVO {
  id?: number
  productNo?: string
  productName?: string
  productId?: number
  productionOrderNo?: string
  productionOrderId?: number
  productBatchNo?: string
  period?: string
  externalTotalAmount?: number
  internalTotalAmount?: number
  diffAmount?: number
  externalMaterialAmount?: number
  internalMaterialAmount?: number
  externalLaborAmount?: number
  internalLaborAmount?: number
  externalOverheadAmount?: number
  internalOverheadAmount?: number
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

export const DualProductCostApi = {
  getProductDualCostPage: async (params: DualProductCostPageReqVO) => {
    return await request.get({ url: `/erp/finance-dual-product-cost/page`, params })
  },
  getProductDualCostItems: async (id: number) => {
    return await request.get({ url: `/erp/finance-dual-product-cost/items?resultId=${id}` })
  },
  rebuildProductDualCost: async (data: {
    productId: number
    productionOrderId?: number
    productBatchNo: string
    period: string
    remark: string
  }) => {
    return await request.post({ url: `/erp/finance-dual-product-cost/rebuild`, data })
  },
  exportExternalProductCost: async (params: DualProductCostPageReqVO) => {
    return await request.download({ url: `/erp/finance-dual-product-cost/export-external`, params })
  },
  exportInternalProductCost: async (params: DualProductCostPageReqVO) => {
    return await request.download({ url: `/erp/finance-dual-product-cost/export-internal`, params })
  }
}
