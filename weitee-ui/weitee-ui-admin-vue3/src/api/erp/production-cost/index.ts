import request from '@/config/axios'

export interface ProductSummaryVO {
  productId?: number
  productName?: string
  productNo?: string
  totalAmount?: number
  materialAmount?: number
  laborAmount?: number
  overheadAmount?: number
}

export interface ProductCostListItem {
  id?: number
  name?: string
}

export const ProductionCostApi = {
  getProductSummaryV2: async (params: {
    accountingMonth?: string
    productId?: number
  }): Promise<ProductSummaryVO[]> => {
    return await request.get({ url: `/erp/production-cost-entry/product-summary-v2`, params })
  },
  getCostProductList: async (): Promise<ProductCostListItem[]> => {
    return await request.get({ url: `/erp/production-cost-entry/cost-products` })
  }
}
