import request from '@/config/axios'

export const PRODUCTION_FINISH_QUALITY_STATUS = {
  TO_INSPECT: 10,
  PARTIAL: 20,
  PASSED: 30,
  REJECTED: 40
} as const

export interface ProductionFinishQualityVO {
  id?: number
  no?: string
  productionOrderId?: number
  productionOrderNo?: string
  sourceOrderId?: number
  sourceItemId?: number
  productId?: number
  reportQty?: number
  qualifiedQty?: number
  unqualifiedQty?: number
  status?: number
  checkerUserId?: number
  checkTime?: Date | string | number
  remark?: string
  createTime?: Date | string | number
}

export interface ProductionFinishQualityPageReqVO {
  pageNo: number
  pageSize: number
  no?: string
  productionOrderId?: number
  sourceOrderId?: number
  status?: number
}

export interface ProductionFinishQualitySubmitReqVO {
  id: number
  qualifiedQty: number
  unqualifiedQty: number
  remark?: string
}

export const ProductionFinishQualityApi = {
  getProductionFinishQualityPage: async (params: ProductionFinishQualityPageReqVO) => {
    return await request.get({ url: '/erp/production-finish-quality/page', params })
  },

  getProductionFinishQuality: async (id: number) => {
    return await request.get({ url: '/erp/production-finish-quality/get?id=' + id })
  },

  submitProductionFinishQuality: async (data: ProductionFinishQualitySubmitReqVO) => {
    return await request.put({ url: '/erp/production-finish-quality/submit', data })
  }
}
