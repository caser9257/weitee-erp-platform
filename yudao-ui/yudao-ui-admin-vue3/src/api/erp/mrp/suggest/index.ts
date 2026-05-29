import request from '@/config/axios'

export interface PurchaseSuggestVO {
  id: number
  planId: number
  projectId?: number
  materialId: number
  materialName?: string
  suggestQty: number
  suggestArrivalDate: string
  grossDemandQty?: number
  availableStockQty?: number
  incomingQty?: number
  wipQty?: number
  reservedStockQty?: number
  safetyStockQty?: number
  netDemandQty?: number
  status: number
  sourceOrderId?: number
  convertPurchaseOrderId?: number
  createTime: string
}

export interface PurchaseSuggestPageReqVO {
  pageNo: number
  pageSize: number
  planId?: number
  materialId?: number
  sourceOrderId?: number
  status?: number
}

export interface ProductionSuggestVO {
  id: number
  planId: number
  projectId?: number
  productId: number
  productName?: string
  suggestQty: number
  suggestStartDate: string
  suggestEndDate: string
  grossDemandQty?: number
  availableStockQty?: number
  incomingQty?: number
  wipQty?: number
  reservedStockQty?: number
  safetyStockQty?: number
  netDemandQty?: number
  status: number
  sourceOrderId?: number
  convertProductionOrderId?: number
  createTime: string
}

export interface ProductionSuggestPageReqVO {
  pageNo: number
  pageSize: number
  planId?: number
  productId?: number
  sourceOrderId?: number
  status?: number
}

export interface PurchaseSuggestConvertReqVO {
  ids: number[]
  supplierId: number | undefined
  accountId?: number
  remark?: string
}

export interface ProductionSuggestConvertReqVO {
  ids: number[]
  remark?: string
}

export const MrpSuggestApi = {
  getPurchaseSuggestPage: async (params: PurchaseSuggestPageReqVO) => {
    return await request.get({ url: '/erp/mrp-suggest/purchase-page', params })
  },

  getProductionSuggestPage: async (params: ProductionSuggestPageReqVO) => {
    return await request.get({ url: '/erp/mrp-suggest/production-page', params })
  },

  confirmPurchaseSuggest: async (ids: number[]) => {
    return await request.post({ url: '/erp/mrp-suggest/purchase-confirm', data: ids })
  },

  confirmProductionSuggest: async (ids: number[]) => {
    return await request.post({ url: '/erp/mrp-suggest/production-confirm', data: ids })
  },

  rejectPurchaseSuggest: async (ids: number[]) => {
    return await request.post({ url: '/erp/mrp-suggest/purchase-reject', data: ids })
  },

  rejectProductionSuggest: async (ids: number[]) => {
    return await request.post({ url: '/erp/mrp-suggest/production-reject', data: ids })
  },

  convertPurchaseSuggest: async (data: PurchaseSuggestConvertReqVO) => {
    return await request.post<number>({ url: '/erp/mrp-suggest/purchase-convert', data })
  },

  convertProductionSuggest: async (data: ProductionSuggestConvertReqVO) => {
    return await request.post<number[]>({ url: '/erp/mrp-suggest/production-convert', data })
  }
}
