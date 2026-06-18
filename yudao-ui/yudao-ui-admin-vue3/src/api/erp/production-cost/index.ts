import request from '@/config/axios'

export interface ProductSummaryParams {
  accountingMonth?: string
  productId?: number
  productName?: string
  productNo?: string
  productionOrderNo?: string
}

export interface ProductSummaryVO {
  productId: number
  productName: string
  productSpec: string
  productUnit: string
  projectCount: number
  productionOrderCount: number
  totalManHour: number
  materialCost: number
  laborCost: number
  depreciationCost: number
  powerCost: number
  otherCost: number
  totalCost: number
  outputQty: number
  unitCost: number
  lastMonthTotalCost: number | null
  costChangeRate: number | null
  costAnomaly: boolean
}

export interface CostTrendParams {
  productId?: number
  startMonth?: string
  endMonth?: string
}

export interface CostTrendVO {
  accountingMonth: string
  productId: number
  productName: string
  totalCost: number
  materialCost: number
  laborCost: number
  depreciationCost: number
  powerCost: number
  otherCost: number
}

export const ProductionCostApi = {
  // 获取产品成本汇总 V2
  getProductSummaryV2: (params: ProductSummaryParams) => {
    return request.get<ProductSummaryVO[]>({ url: '/erp/production-cost-entry/product-summary-v2', params })
  },

  // 获取成本趋势
  getCostTrend: (params: CostTrendParams) => {
    return request.get<CostTrendVO[]>({ url: '/erp/production-cost-entry/cost-trend', params })
  },

  // 获取有成本记录的产品列表
  getCostProductList: () => {
    return request.get<any[]>({ url: '/erp/production-cost-entry/cost-product-list' })
  }
}
