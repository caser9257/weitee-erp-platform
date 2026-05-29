import request from '@/config/axios'

export interface MrpPlanVO {
  id: number
  planNo: string
  planName: string
  planStartDate: string
  planEndDate: string
  status: number
  runTime?: string | number
  operatorId?: number
  remark?: string
  createTime?: string | number
}

export interface MrpPlanPageReqVO {
  pageNo: number
  pageSize: number
  planNo?: string
  planName?: string
  status?: number
  planDate?: string[]
}

export interface MrpPlanSaveReqVO {
  planName: string
  planStartDate: string
  planEndDate: string
  remark?: string
}

export interface MrpResultVO {
  id: number
  traceNodeId?: number
  rootProductId: number
  rootProductName?: string
  materialId: number
  materialName?: string
  tracePathKey?: string
  traceLevel?: number
  parentMaterialId?: number
  bomItemId?: number
  grossDemandQty?: number
  availableStockQty?: number
  incomingQty?: number
  wipQty?: number
  netDemandQty?: number
  policyCode?: string
  policyVersion?: number
  businessType?: string
  mrpEnableFlag?: boolean
  supplyOwner?: string
  skipReason?: string
  suggestType?: string
  suggestDate?: string
  sourceOrderId?: number
  demandDate?: string
}

export interface MrpShortageVO {
  id: number
  traceNodeId?: number
  rootProductId: number
  rootProductName?: string
  materialId: number
  materialName?: string
  tracePathKey?: string
  traceLevel?: number
  parentMaterialId?: number
  bomItemId?: number
  shortageQty?: number
  requiredDate?: string
  sourceOrderId?: number
  substitutes?: MrpShortageSubstituteVO[]
}

export interface MrpShortageSubstituteVO {
  id?: number
  substituteMaterialId: number
  substituteMaterialName?: string
  priority?: number
  replaceRatio?: number
  enableAutoRecommend?: boolean
  sort?: number
  remark?: string
}

export interface MrpResultComponentVO {
  componentCode: string
  componentName?: string
  componentRole?: string
  sequenceNo?: number
  enableFlag?: boolean
  baseQty?: number
  consumedQty?: number
  remainingQty?: number
}

export const MrpPlanApi = {
  getPlanPage: async (params: MrpPlanPageReqVO) => {
    return await request.get({ url: '/erp/mrp-plan/page', params })
  },

  createPlan: async (data: MrpPlanSaveReqVO) => {
    return await request.post<number>({ url: '/erp/mrp-plan/create', data })
  },

  runPlan: async (id: number) => {
    return await request.post<boolean>({ url: `/erp/mrp-plan/run?id=${id}` })
  },

  getPlan: async (id: number) => {
    return await request.get<MrpPlanVO>({ url: `/erp/mrp-plan/get?id=${id}` })
  },

  getResultList: async (planId: number) => {
    return await request.get<MrpResultVO[]>({ url: `/erp/mrp-plan/result?planId=${planId}` })
  },

  getShortageList: async (planId: number) => {
    return await request.get<MrpShortageVO[]>({ url: `/erp/mrp-plan/shortage?planId=${planId}` })
  },

  getResultComponentList: async (resultId: number) => {
    return await request.get<MrpResultComponentVO[]>({
      url: `/erp/mrp-plan/result-component/list?resultId=${resultId}`
    })
  }
}
