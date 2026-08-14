import request from '@/config/axios'

export interface ProductionReportPageReqVO {
  pageNo: number
  pageSize: number
  reportNo?: string
  productionOrderId?: number
  reportType?: number
  reportDate?: string[]
}

export interface ProductionReportItemReqVO {
  productionOrderStepId: number
  deviceId?: number
  workerUserId?: number
  reportedQty: number
  qualifiedQty: number
  scrapQty: number
  workHour?: number
  batchNo?: string
  remark?: string
}

export interface ProductionReportCreateReqVO {
  productionOrderId: number
  reportType: number
  batchNo?: string
  remark?: string
  items: ProductionReportItemReqVO[]
}

export interface ProductionReportVO {
  id: number
  reportNo: string
  productionOrderId: number
  productionOrderNo?: string
  reportDate?: string | Date | number
  reportUserId?: number
  reportType?: number
  batchNo?: string
  status?: number
  remark?: string
  createTime?: string | Date | number
}

export interface ProductionReportItemVO {
  id: number
  reportId?: number
  productionOrderStepId: number
  stepCode?: string
  stepName?: string
  deviceId?: number
  workerUserId?: number
  reportedQty: number
  qualifiedQty: number
  scrapQty: number
  workHour?: number
  batchNo?: string
  remark?: string
}

export interface ProductionOrderStepVO {
  id: number
  productionOrderId: number
  routeStepId?: number
  stepNo: number
  stepCode: string
  stepName: string
  workCenterId?: number
  deviceId?: number
  qcFlag?: boolean
  planQty: number
  reportedQty: number
  qualifiedQty: number
  scrapQty: number
  stepStatus: number
  remark?: string
}

export const ProductionReportApi = {
  getProductionOrderStepList: async (productionOrderId: number) => {
    return await request.get<ProductionOrderStepVO[]>({
      url: '/erp/production-order-step/list',
      params: { productionOrderId }
    })
  },
  startStep: async (id: number) => {
    return await request.put({ url: '/erp/production-order-step/start', params: { id } })
  },
  pauseStep: async (id: number) => {
    return await request.put({ url: '/erp/production-order-step/pause', params: { id } })
  },
  resumeStep: async (id: number) => {
    return await request.put({ url: '/erp/production-order-step/resume', params: { id } })
  },
  finishStep: async (id: number) => {
    return await request.put({ url: '/erp/production-order-step/finish', params: { id } })
  },
  createProductionReport: async (data: ProductionReportCreateReqVO) => {
    return await request.post({ url: '/erp/production-report/create', data })
  },
  getProductionReportPage: async (params: ProductionReportPageReqVO) => {
    return await request.get({ url: '/erp/production-report/page', params })
  },
  getProductionReport: async (id: number) => {
    return await request.get<ProductionReportVO>({ url: `/erp/production-report/get?id=${id}` })
  },
  getProductionReportItems: async (reportId: number) => {
    return await request.get<ProductionReportItemVO[]>({
      url: '/erp/production-report/items',
      params: { reportId }
    })
  }
}
