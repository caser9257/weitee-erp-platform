import request from '@/config/axios'

export interface PlanRuleVO {
  id: number
  productId: number
  productName?: string
  supplyType: string
  replenishMode?: string
  safetyStock?: number
  minOrderQty?: number
  orderMultiple?: number
  fixedOrderQty?: number
  purchaseLeadDay?: number
  makeLeadDay?: number
  enableFlag?: boolean
  shortageWarnFlag?: boolean
  defaultSupplierId?: number
  defaultSupplierName?: string
  remark?: string
  validationStatus?: string
  validationMessage?: string
  createTime?: Date
}

export interface PlanRulePageReqVO {
  pageNo: number
  pageSize: number
  productId?: number
  enableFlag?: boolean
}

export const PlanRuleApi = {
  getPlanRulePage: async (params: PlanRulePageReqVO) => {
    return await request.get({ url: `/erp/bom/rule/page`, params })
  },

  getPlanRule: async (id: number) => {
    return await request.get({ url: `/erp/bom/rule/get?id=` + id })
  },

  createPlanRule: async (data: PlanRuleVO) => {
    return await request.post({ url: `/erp/bom/rule/create`, data })
  },

  updatePlanRule: async (data: PlanRuleVO) => {
    return await request.put({ url: `/erp/bom/rule/update`, data })
  },

  deletePlanRule: async (id: number) => {
    return await request.delete({ url: `/erp/bom/rule/delete?id=` + id })
  }
}
