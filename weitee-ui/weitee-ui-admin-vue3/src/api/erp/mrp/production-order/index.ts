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

export interface ProductionOrderCreateReqVO {
  productId: number
  projectId?: number
  planQty: number
  planStartTime: string
  planEndTime: string
  remark?: string
}

export interface ProductionOrderFinishReqVO {
  id: number
  finishedQty: number
  warehouseId: number
}

export const ProductionOrderApi = {
  getProductionOrderPage: async (params: ProductionOrderPageReqVO) => {
    return await request.get({ url: `/erp/production-order/page`, params })
  },

  createProductionOrder: async (data: ProductionOrderCreateReqVO) => {
    return await request.post({ url: '/erp/production-order/create', data })
  },

  releaseProductionOrder: async (id: number) => {
    return await request.put({ url: `/erp/production-order/release?id=${id}` })
  },

  finishProductionOrder: async (data: ProductionOrderFinishReqVO) => {
    return await request.put({ url: '/erp/production-order/finish', data })
  }
}
