import request from '@/config/axios'

export interface ProductionInboundVO {
  id?: number
  no?: string
  finishQualityId?: number
  finishQualityNo?: string
  productionOrderId?: number
  productionOrderNo?: string
  projectId?: number
  productId?: number
  warehouseId?: number
  warehouseName?: string
  inboundQty?: number
  unitCost?: number
  totalCost?: number
  status?: number
  statusName?: string
  inboundTime?: Date | string | number
  executedBy?: number
  executedTime?: Date | string | number
  remark?: string
  createTime?: Date | string | number
}

export interface ProductionInboundPageReqVO {
  pageNo: number
  pageSize: number
  no?: string
  finishQualityId?: number
  productionOrderId?: number
  status?: number
}

export interface ProductionInboundUpdateReqVO {
  id: number
}

export const ProductionInboundApi = {
  getProductionInboundPage: async (params: ProductionInboundPageReqVO) => {
    return await request.get({ url: '/erp/production-inbound/page', params })
  },

  getProductionInbound: async (id: number) => {
    return await request.get({ url: `/erp/production-inbound/get?id=${id}` })
  },

  executeProductionInbound: async (data: ProductionInboundUpdateReqVO) => {
    return await request.put({ url: '/erp/production-inbound/execute', data })
  },

  cancelProductionInbound: async (data: ProductionInboundUpdateReqVO) => {
    return await request.put({ url: '/erp/production-inbound/cancel', data })
  },

  revertProductionInbound: async (data: ProductionInboundUpdateReqVO) => {
    return await request.put({ url: '/erp/production-inbound/revert', data })
  }
}
