import request from '@/config/axios'

export interface FinancePaymentItemVO {
  id?: number
  apStatementId?: number
  bizType?: number
  bizId?: number
  bizNo?: string
  statementNo?: string
  sourceOrderNo?: string
  totalPrice?: number
  paidPrice?: number
  remainAmount?: number
  paymentPrice?: number
  remark?: string
}

export interface FinancePaymentVO {
  id?: number
  no?: string
  processInstanceId?: string
  supplierId?: number
  supplierName?: string
  paymentTime?: Date | string | number
  financeUserId?: number
  financeUserName?: string
  accountId?: number
  accountName?: string
  totalPrice?: number
  discountPrice?: number
  paymentPrice?: number
  status?: number
  remark?: string
  creator?: string
  creatorName?: string
  createTime?: string
  items?: FinancePaymentItemVO[]
}

export interface FinancePaymentPageReqVO {
  pageNo?: number
  pageSize?: number
  no?: string
  paymentTime?: string[]
  supplierId?: number
  creator?: number | string
  financeUserId?: number
  accountId?: number
  status?: number
  remark?: string
  bizNo?: string
}

export interface FinancePaymentSaveReqVO {
  id?: number
  paymentTime?: Date | string | number
  financeUserId?: number
  supplierId?: number
  accountId?: number
  discountPrice?: number
  remark?: string
  items: Array<{
    id?: number
    apStatementId: number
    bizType: number
    bizId: number
    paidPrice: number
    paymentPrice: number
    remark?: string
  }>
}

export interface FinancePaymentSubmitReqVO {
  id: number
  startUserSelectAssignees?: Record<string, number[]>
}

export interface FinancePaymentCancelApprovalReqVO {
  id: number
  reason: string
}

export const FinancePaymentApi = {
  getFinancePaymentPage: async (params: FinancePaymentPageReqVO) => {
    return await request.get({ url: '/erp/finance-payment/page', params })
  },

  getFinancePayment: async (id: number) => {
    return await request.get({ url: `/erp/finance-payment/get?id=${id}` })
  },

  createFinancePayment: async (data: FinancePaymentSaveReqVO) => {
    return await request.post({ url: '/erp/finance-payment/create', data })
  },

  updateFinancePayment: async (data: FinancePaymentSaveReqVO) => {
    return await request.put({ url: '/erp/finance-payment/update', data })
  },

  updateFinancePaymentStatus: async (id: number, status: number) => {
    return await request.put({
      url: '/erp/finance-payment/update-status',
      params: { id, status }
    })
  },

  submitFinancePayment: async (data: FinancePaymentSubmitReqVO) => {
    return await request.post({
      url: '/erp/finance-payment/submit',
      data
    })
  },

  cancelFinancePaymentApproval: async (data: FinancePaymentCancelApprovalReqVO) => {
    return await request.delete({
      url: '/erp/finance-payment/cancel-approval',
      data
    })
  },

  deleteFinancePayment: async (ids: number[]) => {
    return await request.delete({
      url: '/erp/finance-payment/delete',
      params: {
        ids: ids.join(',')
      }
    })
  },

  exportFinancePayment: async (params: FinancePaymentPageReqVO) => {
    return await request.download({ url: '/erp/finance-payment/export-excel', params })
  }
}
