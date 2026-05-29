import request from '@/config/axios'

export interface ProductionOrderPageReqVO {
  pageNo?: number
  pageSize?: number
  orderNo?: string
  productId?: number
  status?: number
}

export interface ProductionOrderVO {
  id: number
  orderNo: string
  productId: number
  productName?: string
  planQty?: number
  finishedQty?: number
  planStartTime?: string | Date | number
  planEndTime?: string | Date | number
  status?: number
  sourceType?: string
  remark?: string
}

export const ProductionOrderApi = {
  getProductionOrderPage: async (params: ProductionOrderPageReqVO) => {
    return await request.get({ url: `/erp/production-order/page`, params })
  }
}
