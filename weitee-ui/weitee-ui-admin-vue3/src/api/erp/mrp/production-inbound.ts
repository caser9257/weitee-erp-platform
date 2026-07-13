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

export const ProductionInboundApi = {
  getProductionInboundPage: async (params: ProductionInboundPageReqVO) => {
    return await request.get({ url: '/erp/production-inbound/page', params })
  },

  executeProductionInbound: async (id: number) => {
    return await request.put({ url: '/erp/production-inbound/execute', data: { id } })
  },

  revertProductionInbound: async (id: number) => {
    return await request.put({ url: '/erp/production-inbound/revert', data: { id } })
  }
}
