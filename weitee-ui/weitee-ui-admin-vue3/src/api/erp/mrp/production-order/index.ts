import request from '@/config/axios'

export interface ProductionOrderPageReqVO {
  pageNo: number
  pageSize: number
  orderNo?: string
  productId?: number
  status?: number
  planTime?: string[]
}

export interface ProductionOrderSaveReqVO {
  id?: number
  productId: number
  routeId?: number
  routeVersion?: string
  workCenterId?: number
  batchNo?: string
  projectId?: number
  planQty: number
  scrapQty?: number
  planStartTime: string
  planEndTime: string
  remark?: string
}

export interface ProductionOrderFinishReqVO {
  id: number
  finishedQty: number
  warehouseId: number
}

export interface ProductionOrderVO {
  id: number
  orderNo: string
  productId: number
  productName?: string
  routeId?: number
  routeVersion?: string
  workCenterId?: number
  batchNo?: string
  projectId?: number
  planQty?: number
  finishedQty?: number
  scrapQty?: number
  warehouseId?: number
  planStartTime?: string | Date | number
  planEndTime?: string | Date | number
  status?: number
  sourceType?: string
  sourceId?: number
  sourceOrderId?: number
  sourceItemId?: number
  remark?: string
  createTime?: string | Date | number
}

export interface ProductionOrderSummaryVO {
  total: number
  created: number
  released: number
  finished: number
  closed: number
}

export const ProductionOrderApi = {
  getProductionOrderPage: async (params: ProductionOrderPageReqVO) => {
    return await request.get({ url: `/erp/production-order/page`, params })
  },
  getProductionOrderSummary: async () => {
    return await request.get<ProductionOrderSummaryVO>({ url: '/erp/production-order/summary' })
  },
  getProductionOrder: async (id: number) => {
    return await request.get({ url: `/erp/production-order/get?id=${id}` })
  },
  createProductionOrder: async (data: ProductionOrderSaveReqVO) => {
    return await request.post({ url: '/erp/production-order/create', data })
  },
  updateProductionOrder: async (data: ProductionOrderSaveReqVO) => {
    return await request.put({ url: '/erp/production-order/update', data })
  },
  releaseProductionOrder: async (id: number) => {
    return await request.put({ url: '/erp/production-order/release', params: { id } })
  },
  finishProductionOrder: async (data: ProductionOrderFinishReqVO) => {
    return await request.put({ url: '/erp/production-order/finish', data })
  }
}
