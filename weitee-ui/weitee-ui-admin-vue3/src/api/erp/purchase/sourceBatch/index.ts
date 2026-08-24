import request from '@/config/axios'

export interface PurchaseSourceBatchVO {
  id?: number
  batchNo?: string
  productId?: number
  productName?: string
  purchaseOrderId?: number
  purchaseOrderNo?: string
  purchaseOrderItemId?: number
  supplierId?: number
  supplierName?: string
  status?: number
  bizDate?: string
  remark?: string
  createTime?: string
}

export interface PurchaseSourceBatchTraceItemVO {
  id?: number
  executeItemId?: number
  executeId?: number
  executeNo?: string
  purchaseInItemId?: number
  stockBatchId?: number
  productId?: number
  productName?: string
  warehouseId?: number
  warehouseName?: string
  batchNo?: string
  purchaseSourceBatchId?: number
  purchaseSourceBatchNo?: string
  count?: number
  inboundTime?: string
  produceDate?: string
  expireDate?: string
  executeStatus?: number
  executeRemark?: string
  remark?: string
  createTime?: string
}

export interface PurchaseSourceBatchTraceVO {
  sourceBatch?: PurchaseSourceBatchVO
  traceItems?: PurchaseSourceBatchTraceItemVO[]
}

export const PurchaseSourceBatchApi = {
  getTrace: async (id: number) => {
    return await request.get<PurchaseSourceBatchTraceVO>({
      url: '/erp/purchase-source-batch/get-trace',
      params: { id }
    })
  }
}
