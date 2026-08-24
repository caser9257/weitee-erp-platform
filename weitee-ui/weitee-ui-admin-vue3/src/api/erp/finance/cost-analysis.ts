import request from '@/config/axios'

export interface ProductionCostProductVO {
  productId?: number
  productName?: string
}

export interface ProductionCostTrendReqVO {
  productId?: number
  startMonth?: string
  endMonth?: string
  dimension?: 'month' | 'quarter'
}

export interface ProductionCostTrendRespVO {
  periods?: string[]
  materialCosts?: number[]
  laborCosts?: number[]
  depreciationCosts?: number[]
  powerCosts?: number[]
  otherCosts?: number[]
  totalCosts?: number[]
  outputQtys?: number[]
  unitCosts?: number[]
  compositionData?: {
    materialCost?: number
    laborCost?: number
    depreciationCost?: number
    powerCost?: number
    otherCost?: number
    totalCost?: number
  }
  productCompareData?: Array<{
    productId?: number
    productName?: string
    materialCost?: number
    laborCost?: number
    overheadCost?: number
    totalCost?: number
  }>
  productOrderData?: Record<string, { orderNo?: string; orderId?: number }>
}

export const FinanceCostAnalysisApi = {
  getCostProducts: async (): Promise<ProductionCostProductVO[]> => {
    return await request.get({ url: '/erp/production-cost-entry/cost-products' })
  },

  getCostTrend: async (
    params: ProductionCostTrendReqVO
  ): Promise<ProductionCostTrendRespVO> => {
    return await request.get({ url: '/erp/production-cost-entry/cost-trend', params })
  }
}
