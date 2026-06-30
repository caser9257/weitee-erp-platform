import request from '@/config/axios'

export interface WorkOrderVO {
  id?: number
  no?: string
  productId?: number
  productName?: string
  projectId?: number
  planId?: number
  status?: number
  planQuantity?: number
  producedQuantity?: number
  startTime?: Date | string
  endTime?: Date | string
  remark?: string
  createTime?: Date | string
}

export interface WorkOrderFinishReqVO {
  id: number
  producedQuantity?: number
  remark?: string
}

export const WorkOrderApi = {
  getWorkOrderPage: async (params: any) => {
    return await request.get({ url: '/erp/production-order/page', params })
  },

  getWorkOrder: async (id: number) => {
    return await request.get({ url: `/erp/production-order/get?id=${id}` })
  },

  createWorkOrder: async (data: WorkOrderVO) => {
    return await request.post({ url: '/erp/production-order/create', data })
  },

  updateWorkOrder: async (data: WorkOrderVO) => {
    return await request.put({ url: '/erp/production-order/update', data })
  },

  releaseWorkOrder: async (id: number) => {
    return await request.put({ url: `/erp/production-order/release?id=${id}` })
  },

  finishWorkOrder: async (data: WorkOrderFinishReqVO) => {
    return await request.put({ url: '/erp/production-order/finish', data })
  }
}
