import request from '@/config/axios'

export interface StockReservationVO {
  id: number
  planId: number
  planNo?: string
  projectId?: number
  projectNo?: string
  projectName?: string
  productId: number
  productName?: string
  sourceOrderId?: number
  sourceOrderNo?: string
  sourceItemId?: number
  reservedQty?: number
  status?: number
  createTime?: string | number
}

export interface StockReservationSummaryVO {
  productId: number
  productName?: string
  stockQty?: number
  activeReservedQty?: number
  availableQty?: number
  activeProjectCount?: number
  activeReservationCount?: number
  lastReservedTime?: string | number
  projectDistributionItems?: string[]
  projectDistributionMoreCount?: number
}

export interface StockReservationProjectSummaryVO {
  productId: number
  projectId?: number
  projectNo?: string
  projectName?: string
  activeReservedQty?: number
  activeReservationCount?: number
  sourceOrderCount?: number
  lastReservedTime?: string | number
}

export interface StockReservationPageReqVO {
  pageNo: number
  pageSize: number
  planId?: number
  projectId?: number
  productId?: number
  sourceOrderId?: number
  status?: number
}

export interface StockReservationSummaryPageReqVO {
  pageNo: number
  pageSize: number
  productId?: number
}

export const StockReservationApi = {
  getStockReservationPage: async (params: StockReservationPageReqVO) => {
    return await request.get<{ list: StockReservationVO[]; total: number }>({
      url: '/erp/mrp-stock-reservation/page',
      params
    })
  },

  getStockReservationSummaryPage: async (params: StockReservationSummaryPageReqVO) => {
    return await request.get<{ list: StockReservationSummaryVO[]; total: number }>({
      url: '/erp/mrp-stock-reservation/summary-page',
      params
    })
  },

  getStockReservationProjectSummaryList: async (productId: number) => {
    return await request.get<StockReservationProjectSummaryVO[]>({
      url: '/erp/mrp-stock-reservation/summary-project-list',
      params: { productId }
    })
  }
}
