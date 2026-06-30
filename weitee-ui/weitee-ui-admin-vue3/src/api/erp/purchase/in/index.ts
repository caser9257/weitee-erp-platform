import request from '@/config/axios'

export interface PurchaseInBatchVO {
  id?: number
  executeItemId?: number
  purchaseInItemId?: number
  stockBatchId?: number
  purchaseSourceBatchId?: number
  batchNo?: string
  purchaseSourceBatchNo?: string
  count?: number
  inboundTime?: Date | string | number
  produceDate?: string
  expireDate?: string
  remark?: string
}

export interface PurchaseInItemVO {
  id?: number
  orderItemId?: number
  warehouseId?: number
  productId?: number
  purchaseSourceBatchId?: number
  purchaseSourceBatchNo?: string
  productUnitId?: number
  productPrice?: number
  engineeringFee?: number
  pricingBomId?: number
  pricingBomVersion?: string
  count?: number
  qaPassCount?: number
  qaRejectCount?: number
  stockInCount?: number
  remainingStockInCount?: number
  qaRemark?: string
  taxPercent?: number
  taxPrice?: number
  remark?: string
  productName?: string
  productBarCode?: string
  productUnitName?: string
  batchControlFlag?: boolean
  stockCount?: number
  totalCount?: number
  totalPrice?: number
}

export interface PurchaseInAuditLogVO {
  id?: number
  inId?: number
  actionType?: string
  beforeStatus?: number
  afterStatus?: number
  reason?: string
  operatorId?: number
  operatorName?: string
  operatorNickname?: string
  taskName?: string
  createTime?: Date | string | number
}

export interface PurchaseInStockExecuteItemVO {
  id?: number
  executeId?: number
  purchaseInItemId?: number
  productId?: number
  warehouseId?: number
  count?: number
  remark?: string
  productName?: string
  productBarCode?: string
  productUnitName?: string
  batches?: PurchaseInBatchVO[]
}

export interface PurchaseInStockExecuteVO {
  id?: number
  no?: string
  status?: number
  remark?: string
  createTime?: Date | string | number
  creator?: string
  creatorName?: string
  items?: PurchaseInStockExecuteItemVO[]
}

export interface PurchaseInVO {
  id?: number
  no?: string
  status?: number
  qaStatus?: number
  stockInStatus?: number
  processInstanceId?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  inTime?: Date | string | number
  orderId?: number
  orderNo?: string
  totalCount?: number
  totalPrice?: number
  paymentPrice?: number
  totalProductPrice?: number
  totalTaxPrice?: number
  discountPercent?: number
  discountPrice?: number
  otherPrice?: number
  fileUrl?: string
  remark?: string
  creator?: string
  creatorName?: string
  createTime?: Date | string | number
  lastRejectReason?: string
  lastRejectTime?: Date | string | number
  lastRejectUserId?: number
  qaTime?: Date | string | number
  qaUserId?: number
  qaUserNickname?: string
  qaRemark?: string
  qaPassCount?: number
  qaRejectCount?: number
  stockInCount?: number
  remainingStockInCount?: number
  stockInTime?: Date | string | number
  stockInUserId?: number
  stockInUserNickname?: string
  productNames?: string
  auditLogs?: PurchaseInAuditLogVO[]
  stockExecuteList?: PurchaseInStockExecuteVO[]
  items: PurchaseInItemVO[]
}

export interface PurchaseInPrintSourceAttachmentVO {
  name?: string
  url?: string
}

export interface PurchaseInPrintDataVO {
  purchaseIn?: PurchaseInVO
  sourceAttachments?: PurchaseInPrintSourceAttachmentVO[]
}

export interface PurchaseInSubmitReqVO {
  id: number
  startUserSelectAssignees?: Record<string, number[]>
}

export interface PurchaseInCancelApprovalReqVO {
  id: number
  reason: string
}

export interface PurchaseInQualityCheckReqVO {
  id: number
  remark?: string
  items: PurchaseInQualityCheckItemVO[]
}

export interface PurchaseInConfirmStockInReqVO {
  id: number
}

export interface PurchaseInStockExecuteCreateBatchVO {
  batchNo: string
  count: number
  inboundTime: string
  produceDate?: string
  expireDate?: string
  remark?: string
}

export interface PurchaseInStockExecuteCreateItemVO {
  purchaseInItemId: number
  count: number
  remark?: string
  batches?: PurchaseInStockExecuteCreateBatchVO[]
}

export interface PurchaseInStockExecuteCreateReqVO {
  purchaseInId: number
  remark?: string
  items: PurchaseInStockExecuteCreateItemVO[]
}

export interface PurchaseInQualityCheckItemVO {
  id: number
  qaPassCount: number
  qaRejectCount: number
  qaRemark?: string
}

export interface PurchaseInBatchUpdateReqVO {
  ids: number[]
  fieldKey: string
  mode: string
  value: string
}

export interface PurchaseInBatchUpdateResultVO {
  successCount: number
  failureCount: number
  updatedIds: number[]
  failedItems: Array<{
    id: number
    message: string
  }>
}

  export const PurchaseInApi = {
  getPurchaseInPage: async (params: any) => {
    return await request.get({ url: `/erp/purchase-in/page`, params })
  },

  getPurchaseIn: async (id: number) => {
    return await request.get({ url: `/erp/purchase-in/get?id=` + id })
  },

  getPurchaseInPrintData: async (id: number) => {
    return await request.get<PurchaseInPrintDataVO>({
      url: `/erp/purchase-in/get-print-data?id=` + id
    })
  },

  createPurchaseIn: async (data: PurchaseInVO) => {
    return await request.post({ url: `/erp/purchase-in/create`, data })
  },

  updatePurchaseIn: async (data: PurchaseInVO) => {
    return await request.put({ url: `/erp/purchase-in/update`, data })
  },

  batchUpdatePurchaseIn: async (data: PurchaseInBatchUpdateReqVO) => {
    return await request.put<PurchaseInBatchUpdateResultVO>({
      url: `/erp/purchase-in/batch-update`,
      data
    })
  },

  updatePurchaseInStatus: async (id: number, status: number) => {
    return await request.put({
      url: `/erp/purchase-in/update-status`,
      params: {
        id,
        status
      }
    })
  },

  submitPurchaseIn: async (data: PurchaseInSubmitReqVO) => {
    return await request.post({
      url: `/erp/purchase-in/submit`,
      data
    })
  },

  cancelPurchaseInApproval: async (data: PurchaseInCancelApprovalReqVO) => {
    return await request.delete({
      url: `/erp/purchase-in/cancel-approval`,
      data
    })
  },

  qualityCheckPurchaseIn: async (data: PurchaseInQualityCheckReqVO) => {
    return await request.post({
      url: `/erp/purchase-in/quality-check`,
      data
    })
  },

  confirmStockIn: async (data: PurchaseInConfirmStockInReqVO) => {
    return await request.post({
      url: `/erp/purchase-in/confirm-stock-in`,
      data
    })
  },

  createStockExecute: async (data: PurchaseInStockExecuteCreateReqVO) => {
    return await request.post({
      url: `/erp/purchase-in/stock-execute/create`,
      data
    })
  },

  deletePurchaseIn: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/purchase-in/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  exportPurchaseIn: async (params: any) => {
    return await request.download({ url: `/erp/purchase-in/export-excel`, params })
  }
}
