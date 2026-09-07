import request from '@/config/axios'

// ERP 采购退货 VO
export interface PurchaseReturnVO {
  id: number // 采购退货编号
  no: string // 采购退货号
  supplierId?: number // 供应商编号
  supplierName?: string // 供应商名称
  accountId?: number // 结算账户编号
  returnTime: Date // 退货时间
  totalCount: number // 合计数量
  totalPrice: number // 合计金额，单位：元
  totalProductPrice?: number // 合计产品价格，单位：元
  totalTaxPrice?: number // 合计税额，单位：元
  discountPercent?: number // 优惠率，百分比
  discountPrice?: number // 优惠金额，单位：元
  otherPrice?: number // 其它金额，单位：元
  refundPrice?: number // 已退款金额，单位：元
  orderId?: number // 采购订单编号
  orderNo?: string // 采购订单号
  fileUrl?: string // 附件地址
  status: number // 状态
  statusName?: string // 状态名称
  processInstanceId?: string // 审批流程实例编号
  remark: string // 备注
  creator?: string // 创建人
  creatorName?: string // 创建人名称
  createTime?: Date | string | number // 创建时间
  qualityId?: number // 来源质检单编号
  items?: PurchaseReturnItemVO[]
  productNames?: string // 产品信息
}

export interface PurchaseReturnSubmitReqVO {
  id: number
  startUserSelectAssignees?: Record<string, number[]>
}

export interface PurchaseReturnCancelApprovalReqVO {
  id: number
  reason: string
}

export interface PurchaseReturnItemVO {
  id?: number
  orderItemId?: number
  warehouseId?: number
  warehouseName?: string
  productId?: number
  productPrice?: number
  count?: number
  taxPercent?: number
  taxPrice?: number
  remark?: string
  productName?: string
  productBarCode?: string
  productUnitName?: string
  stockCount?: number
}

export interface PurchaseReturnPrintSourceAttachmentVO {
  name?: string
  url?: string
}

export interface PurchaseReturnPrintFinancialFactsVO {
  accountId?: number
  accountName?: string
  totalCount?: number
  totalProductPrice?: number
  totalTaxPrice?: number
  discountPercent?: number
  discountPrice?: number
  otherPrice?: number
  totalPrice?: number
  refundPrice?: number
  remainingPrice?: number
  refundProgressName?: string
}

export interface PurchaseReturnPrintDataVO {
  purchaseReturn?: PurchaseReturnVO
  financialFacts?: PurchaseReturnPrintFinancialFactsVO
  sourceAttachments?: PurchaseReturnPrintSourceAttachmentVO[]
}

// ERP 采购退货 API
export const PurchaseReturnApi = {
  // 查询采购退货分页
  getPurchaseReturnPage: async (params: any) => {
    return await request.get({ url: `/erp/purchase-return/page`, params })
  },

  // 查询采购退货详情
  getPurchaseReturn: async (id: number) => {
    return await request.get({ url: `/erp/purchase-return/get?id=` + id })
  },

  getPurchaseReturnPrintData: async (id: number) => {
    return await request.get<PurchaseReturnPrintDataVO>({
      url: `/erp/purchase-return/get-print-data?id=` + id
    })
  },

  // 新增采购退货
  createPurchaseReturn: async (data: PurchaseReturnVO) => {
    return await request.post({ url: `/erp/purchase-return/create`, data })
  },

  // 修改采购退货
  updatePurchaseReturn: async (data: PurchaseReturnVO) => {
    return await request.put({ url: `/erp/purchase-return/update`, data })
  },

  // 更新采购退货的状态
  updatePurchaseReturnStatus: async (id: number, status: number) => {
    return await request.put({
      url: `/erp/purchase-return/update-status`,
      params: {
        id,
        status
      }
    })
  },

  // 提交采购退货审批
  submitPurchaseReturn: async (data: PurchaseReturnSubmitReqVO) => {
    return await request.post({ url: `/erp/purchase-return/submit`, data })
  },

  // 撤回采购退货审批
  cancelPurchaseReturnApproval: async (data: PurchaseReturnCancelApprovalReqVO) => {
    return await request.delete({ url: `/erp/purchase-return/cancel-approval`, data })
  },

  // 删除采购退货
  deletePurchaseReturn: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/purchase-return/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  // 导出采购退货 Excel
  exportPurchaseReturn: async (params: any) => {
    return await request.download({ url: `/erp/purchase-return/export-excel`, params })
  }
}
