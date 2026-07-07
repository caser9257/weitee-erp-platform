import request from '@/config/axios'

export interface PurchaseOrderRejectLogVO {
  reason: string
  rejectTime?: Date
  rejectUserName?: string
  rejectUserNickname?: string
  rejectUserId?: number
  creatorName?: string
  createTime?: Date
}

export interface PurchaseOrderAuditLogVO {
  actionType: string
  beforeStatus?: number
  afterStatus?: number
  reason?: string
  operatorId?: number
  operatorName?: string
  operatorNickname?: string
  taskName?: string
  createTime?: Date
}

export interface PurchaseOrderItemVO {
  id?: number
  productId?: number
  projectId?: number
  productUnitId?: number
  productPrice?: number
  engineeringFee?: number
  pricingBomId?: number
  pricingBomVersion?: string
  count?: number
  taxPercent?: number
  taxPrice?: number
  totalProductPrice?: number
  totalPrice?: number
  remark?: string
  inCount?: number
  returnCount?: number
  productName?: string
  productBarCode?: string
  productStandard?: string
  productUnitName?: string
  projectName?: string
  taxIncludedPrice?: number
  deliveryDate?: Date | string
  paymentAllocatedAmount?: number
  relatedCount?: number
  invoicedCount?: number
  auditorName?: string
  stockCount?: number
  totalCount?: number
  orderItemId?: number
}

export interface PurchaseOrderVO {
  id: number
  no: string
  status: number
  supplierId: number
  projectId?: number
  supplierName?: string
  accountId?: number
  orderTime: Date
  totalCount?: number
  totalPrice?: number
  totalProductPrice?: number
  totalTaxPrice?: number
  discountPercent?: number
  discountPrice?: number
  depositPrice?: number
  paymentPrice?: number
  paymentStatus?: number
  fileUrl?: string
  remark?: string
  creator?: string
  creatorName?: string
  businessOwnerName?: string
  sourceOrderNos?: string
  hasPendingPurchaseIn?: boolean
  pendingPurchaseInId?: number
  createTime?: Date
  productNames?: string
  inCount?: number
  returnCount?: number
  processInstanceId?: string
  lastRejectReason?: string
  lastRejectTime?: Date
  lastRejectUserId?: number
  sourceType?: string
  rejectLogs?: PurchaseOrderRejectLogVO[]
  operationLogs?: PurchaseOrderAuditLogVO[]
  approvalLogs?: PurchaseOrderAuditLogVO[]
  auditLogs?: PurchaseOrderAuditLogVO[]
  items: PurchaseOrderItemVO[]
}

export interface PurchaseOrderPageReqVO {
  pageNo: number
  pageSize: number
  no?: string
  supplierId?: number
  productId?: number
  sourceOrderId?: number
  orderTime?: string[]
  status?: number
  remark?: string
  creator?: string
  inStatus?: number | string
  returnStatus?: number | string
}

export interface PurchaseOrderBatchUpdateReqVO {
  ids: number[]
  fieldKey: string
  mode: string
  value: string
}

export interface PurchaseOrderBatchUpdateResultVO {
  successCount: number
  failureCount: number
  updatedIds: number[]
  failedItems: Array<{
    id: number
    message: string
  }>
}

export interface PurchaseOrderSubmitReqVO {
  id: number
  startUserSelectAssignees?: Record<string, number[]>
}

export interface PurchaseOrderCancelApprovalReqVO {
  id: number
  reason: string
}

export const PurchaseOrderApi = {
  getPurchaseOrderPage: async (params: PurchaseOrderPageReqVO) => {
    return await request.get({ url: `/erp/purchase-order/page`, params })
  },

  getPurchaseOrder: async (id: number) => {
    return await request.get({ url: `/erp/purchase-order/get?id=` + id })
  },

  createPurchaseOrder: async (data: PurchaseOrderVO) => {
    return await request.post({ url: `/erp/purchase-order/create`, data })
  },

  updatePurchaseOrder: async (data: PurchaseOrderVO) => {
    return await request.put({ url: `/erp/purchase-order/update`, data })
  },

  batchUpdatePurchaseOrder: async (data: PurchaseOrderBatchUpdateReqVO) => {
    return await request.put<PurchaseOrderBatchUpdateResultVO>({
      url: `/erp/purchase-order/batch-update`,
      data
    })
  },

  updatePurchaseOrderStatus: async (id: number, status: number) => {
    return await request.put({
      url: `/erp/purchase-order/update-status`,
      params: {
        id,
        status
      }
    })
  },

  submitPurchaseOrder: async (data: PurchaseOrderSubmitReqVO) => {
    return await request.post({
      url: `/erp/purchase-order/submit`,
      data
    })
  },

  cancelPurchaseOrderApproval: async (data: PurchaseOrderCancelApprovalReqVO) => {
    return await request.delete({
      url: `/erp/purchase-order/cancel-approval`,
      data
    })
  },

  deletePurchaseOrder: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/purchase-order/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  exportPurchaseOrder: async (params: any) => {
    return await request.download({ url: `/erp/purchase-order/export-excel`, params })
  }
}
