import request from '@/config/axios'

// ERP 销售订单 VO
export interface SaleOrderRejectLogVO {
  reason: string
  rejectTime?: Date
  rejectUserName?: string
  rejectUserNickname?: string
  rejectUserId?: number
  creatorName?: string
  createTime?: Date
}

export interface SaleOrderAuditLogVO {
  actionType: string
  beforeStatus?: number
  afterStatus?: number
  reason?: string
  operatorId?: number
  operatorName?: string
  operatorNickname?: string
  createTime?: Date
}

export interface SaleOrderVO {
  id: number // 订单编号
  no: string // 销售订单号
  customerId: number // 客户编号
  customerName?: string // 客户名称
  orderTime: Date // 订单时间
  totalCount: number // 合计数量
  totalCountUnitName?: string // 合计数量单位
  totalPrice: number // 合计金额，单位：元
  status: number // 状态
  remark: string // 备注
  outCount: number // 销售出库数量
  returnCount: number // 销售退货数量
  deliveryReadyStatus?: string // 交付就绪状态
  processInstanceId?: string // BPM 流程实例编号
  projectId?: number // 项目编号
  projectName?: string // 项目名称
  saleUserId?: number // 销售员编号
  saleUserName?: string // 销售员名称
  businessType?: string // 业务类型
  sourceProjectId?: number // 来源研发项目编号
  settlementType?: string // 结算类型
  sourceProductId?: number // 来源产品编号
  deliveryDate?: Date // 交期
  creator?: string // 创建人编号
  closureSummary?: SaleOrderClosureSummaryVO | null // 闭环摘要
  lastRejectReason?: string // 最近一次驳回原因
  lastRejectTime?: Date // 最近一次驳回时间
  rejectLogs?: SaleOrderRejectLogVO[] // 驳回历史
  auditLogs?: SaleOrderAuditLogVO[] // 审批流转历史
}

export interface SaleOrderClosureSummaryVO {
  saleOrderId: number
  saleOrderNo: string
  remainingShipQty?: number
  productionQualifiedQty?: number
  deliveryReadyStatus?: string
  closureStage?: string
  blockerCodes?: string[]
  purchaseSuggestCount?: number
  purchaseConfirmedSuggestCount?: number
  purchaseConvertedSuggestCount?: number
  purchaseRejectedSuggestCount?: number
  productionSuggestCount?: number
  productionConfirmedSuggestCount?: number
  productionConvertedSuggestCount?: number
  productionRejectedSuggestCount?: number
  purchaseOrderCount?: number
  approvedPurchaseOrderCount?: number
  purchaseInCount?: number
  approvedPurchaseInCount?: number
  pendingQaPurchaseInCount?: number
  pendingStockInPurchaseInCount?: number
  stockedPurchaseInCount?: number
  noNeedStockInPurchaseInCount?: number
}

export interface SaleOrderClosurePageReqVO {
  pageNo: number
  pageSize: number
  no?: string
  customerId?: number
  projectId?: number
  saleUserId?: number
  status?: number
  deliveryReadyStatus?: string
}

export interface SaleOrderClosurePageVO {
  id: number
  no: string
  customerId?: number
  customerName?: string
  projectId?: number
  projectName?: string
  saleUserId?: number
  saleUserName?: string
  status?: number
  orderTime?: Date | string
  deliveryDate?: Date | string
  deliveryReadyStatus?: string
  closureSummary?: SaleOrderClosureSummaryVO | null
}

export interface SaleOrderSubmitReqVO {
  id: number
  startUserSelectAssignees?: Record<string, number[]>
}

export interface SaleOrderCancelApprovalReqVO {
  id: number
  reason: string
}

export interface SaleOrderBatchUpdateReqVO {
  ids: number[]
  fieldKey: string
  mode: 'overwrite'
  value: string
}

export interface SaleOrderBatchUpdateResultVO {
  successCount: number
  failureCount: number
  updatedIds: number[]
  failedItems: Array<{
    id: number
    message: string
  }>
}

// ERP 销售订单 API
export const SaleOrderApi = {
  // 查询销售订单分页
  getSaleOrderPage: async (params: any) => {
    return await request.get({ url: `/erp/sale-order/page`, params })
  },

  // 查询销售订单详情
  getSaleOrder: async (id: number) => {
    return await request.get({ url: `/erp/sale-order/get?id=` + id })
  },

  // 查询销售闭环工作台分页
  getSaleOrderClosureSummaryPage: async (params: SaleOrderClosurePageReqVO) => {
    return await request.get({ url: `/erp/sale-order/get-closure-summary-page`, params })
  },

  // 新增销售订单
  createSaleOrder: async (data: SaleOrderVO) => {
    return await request.post({ url: `/erp/sale-order/create`, data })
  },

  // 修改销售订单
  updateSaleOrder: async (data: SaleOrderVO) => {
    return await request.put({ url: `/erp/sale-order/update`, data })
  },

  // 批量修改销售订单
  batchUpdateSaleOrder: async (data: SaleOrderBatchUpdateReqVO) => {
    return await request.put<SaleOrderBatchUpdateResultVO>({
      url: `/erp/sale-order/batch-update`,
      data
    })
  },

  // 更新销售订单的状态
  updateSaleOrderStatus: async (data: { id: number; status: number; reason?: string }) => {
    return await request.put({
      url: `/erp/sale-order/update-status`,
      data
    })
  },

  // 提交销售订单审批
  submitSaleOrder: async (data: SaleOrderSubmitReqVO) => {
    return await request.post({
      url: `/erp/sale-order/submit`,
      data
    })
  },

  // 撤回销售订单审批
  cancelSaleOrderApproval: async (data: SaleOrderCancelApprovalReqVO) => {
    return await request.delete({
      url: `/erp/sale-order/cancel-approval`,
      data
    })
  },

  // 删除销售订单
  deleteSaleOrder: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/sale-order/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  // 导出售订单 Excel
  exportSaleOrder: async (params: any) => {
    return await request.download({ url: `/erp/sale-order/export-excel`, params })
  }
}
