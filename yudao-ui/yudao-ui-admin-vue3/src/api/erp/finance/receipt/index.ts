import request from '@/config/axios'

export interface FinanceReceiptItemVO {
  id?: number
  receiptId?: number
  saleOutId?: number
  saleOutNo?: string
  saleOrderId?: number
  saleOrderNo?: string
  receiptPrice?: number
  remark?: string
}

// ERP 收款单 VO
export interface FinanceReceiptVO {
  id?: number // 收款单编号
  no?: string // 收款单号
  customerId?: number // 客户编号
  customerName?: string // 客户名称
  financeUserId?: number // 财务人员编号
  financeUserName?: string // 财务人员名称
  accountId?: number // 账户编号
  accountName?: string // 账户名称
  receiptTime?: Date | number | string // 收款时间
  totalPrice?: number // 合计金额，单位：元
  discountPrice?: number // 优惠金额
  receiptPrice?: number // 实际收款
  status?: number // 状态
  remark?: string // 备注
  fileUrl?: string // 附件
  bizNo?: string // 业务单号
  creatorName?: string // 创建人
  createTime?: Date | string
  items?: FinanceReceiptItemVO[]
}

export interface FinanceReceiptPageReqVO {
  pageNo: number
  pageSize: number
  no?: string
  receiptTime?: (string | number)[]
  customerId?: number
  creator?: number
  financeUserId?: number
  accountId?: number
  status?: number
  remark?: string
  bizNo?: string
}

// ERP 收款单 API
export const FinanceReceiptApi = {
  // 查询收款单分页
  getFinanceReceiptPage: async (params: FinanceReceiptPageReqVO) => {
    return await request.get({ url: `/erp/finance-receipt/page`, params })
  },

  // 查询收款单详情
  getFinanceReceipt: async (id: number) => {
    return await request.get({ url: `/erp/finance-receipt/get?id=` + id })
  },

  // 新增收款单
  createFinanceReceipt: async (data: FinanceReceiptVO) => {
    return await request.post({ url: `/erp/finance-receipt/create`, data })
  },

  // 修改收款单
  updateFinanceReceipt: async (data: FinanceReceiptVO) => {
    return await request.put({ url: `/erp/finance-receipt/update`, data })
  },

  // 更新收款单的状态
  updateFinanceReceiptStatus: async (id: number, status: number) => {
    return await request.put({
      url: `/erp/finance-receipt/update-status`,
      params: {
        id,
        status
      }
    })
  },

  // 删除收款单
  deleteFinanceReceipt: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/finance-receipt/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  // 导出收款单 Excel
  exportFinanceReceipt: async (params: FinanceReceiptPageReqVO) => {
    return await request.download({ url: `/erp/finance-receipt/export-excel`, params })
  }
}
