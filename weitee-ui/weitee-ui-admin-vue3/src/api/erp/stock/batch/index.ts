import request from '@/config/axios'

export interface StockBatchVO {
  id: number
  productId: number
  productName?: string
  materialCode?: string
  unitName?: string
  warehouseId: number
  warehouseName?: string
  batchNo: string
  inboundTime?: string
  produceDate?: string
  expireDate?: string
  totalQty: number
  availableQty: number
  lockedQty: number
  virtualFlag?: boolean
  sourceBizType?: string
  sourceBizNo?: string
  purchaseSourceBatchId?: number
  purchaseSourceBatchNo?: string
  remark?: string
}

export interface StockBatchAllocationVO {
  id: number
  bizType: number
  bizId: number
  bizItemId?: number
  bizNo?: string
  productId: number
  warehouseId: number
  stockBatchId: number
  batchNo: string
  outQty: number
  inboundTime?: string
  produceDate?: string
  expireDate?: string
  remark?: string
}

export interface StockBatchReservationVO {
  id: number
  bizType: number
  bizId: number
  bizItemId?: number
  bizNo?: string
  productId: number
  productName?: string
  materialCode?: string
  unitName?: string
  warehouseId: number
  warehouseName?: string
  stockBatchId: number
  batchNo: string
  reservedQty: number
  inboundTime?: string
  produceDate?: string
  expireDate?: string
  remark?: string
}

export interface StockBatchRecordVO {
  id: number
  productId: number
  productName?: string
  materialCode?: string
  unitName?: string
  warehouseId: number
  warehouseName?: string
  stockBatchId: number
  batchNo: string
  count: number
  afterAvailableQty: number
  bizType: number
  bizId?: number
  bizItemId?: number
  bizNo?: string
  remark?: string
  createTime?: string
}

export const StockBatchApi = {
  getStockBatchPage: async (params: any) => {
    return await request.get({ url: '/erp/stock-batch/page', params })
  },

  getAllocationListByStockBatch: async (stockBatchId: number) => {
    return await request.get({
      url: '/erp/stock-batch/allocation-by-stock-batch',
      params: { stockBatchId }
    })
  },

  getReservationListByStockBatch: async (stockBatchId: number) => {
    return await request.get({
      url: '/erp/stock-batch/reservation-by-stock-batch',
      params: { stockBatchId }
    })
  },

  getStockBatchRecordPage: async (params: any) => {
    return await request.get({ url: '/erp/stock-batch/record-page', params })
  }
}
