import request from '@/config/axios'

export interface StepQualityPageReqVO {
  pageNo: number
  pageSize: number
  no?: string
  productionOrderId?: number
  productionOrderStepId?: number
  status?: number
  checkTime?: string[]
}

export interface StepQualityVO {
  id: number
  no: string
  productionOrderId: number
  productionOrderNo?: string
  productionOrderStepId: number
  reportId?: number
  stepNo?: number
  stepCode?: string
  stepName?: string
  reportQty: number
  qualifiedQty: number
  unqualifiedQty: number
  status?: number
  checkerUserId?: number
  checkTime?: string | Date | number
  remark?: string
  createTime?: string | Date | number
}

export const STEP_QUALITY_STATUS = {
  TO_INSPECT: 10,
  PARTIAL: 20,
  PASSED: 30,
  REJECTED: 40
} as const

export const StepQualityApi = {
  getStepQualityPage: async (params: StepQualityPageReqVO) => {
    return await request.get({ url: '/erp/production-step-quality/page', params })
  },
  getStepQuality: async (id: number) => {
    return await request.get<StepQualityVO>({ url: `/erp/production-step-quality/get?id=${id}` })
  },
  submitStepQuality: async (data: {
    id: number
    qualifiedQty: number
    unqualifiedQty: number
    remark?: string
  }) => {
    return await request.put({ url: '/erp/production-step-quality/submit', params: data })
  }
}
