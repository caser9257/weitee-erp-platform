import request from '@/config/axios'

export interface ErpFinanceVoucherEntryVO {
  id?: number
  entryNo?: number
  summary?: string
  subjectCode?: string
  subjectName?: string
  debitAmount?: number | string
  creditAmount?: number | string
}

export interface ErpFinanceVoucherVO {
  id?: number
  voucherNo?: string
  ledgerId?: number
  ledgerName?: string
  periodId?: number
  periodCode?: string
  templateId?: number
  templateName?: string
  bizType?: number
  bizTypeName?: string
  bizId?: number
  bizNo?: string
  voucherTime?: string
  status?: number
  statusName?: string
  totalDebitAmount?: number | string
  totalCreditAmount?: number | string
  approveUserId?: number
  approveTime?: string
  postUserId?: number
  postTime?: string
  reverseUserId?: number
  reverseTime?: string
  reverseVoucherId?: number
  reverseVoucherNo?: string
  reverseFromVoucherId?: number
  reverseFromVoucherNo?: string
  reverseRemark?: string
  remark?: string
  createTime?: string
  entries?: ErpFinanceVoucherEntryVO[]
}

export interface ErpFinanceVoucherPageReqVO {
  pageNo?: number
  pageSize?: number
  ledgerId?: number
  periodId?: number
  bizType?: number
  bizNo?: string
  voucherNo?: string
  status?: number
  voucherTime?: string[]
}

export const FinanceVoucherApi = {
  getVoucherPage: async (params: ErpFinanceVoucherPageReqVO) => {
    return await request.get({ url: '/erp/finance-voucher/page', params })
  },

  getVoucher: async (id: number) => {
    return await request.get({ url: `/erp/finance-voucher/get?id=${id}` })
  },

  getVoucherByBiz: async (params: { ledgerId: number; bizType: number; bizId: number }) => {
    return await request.get({ url: '/erp/finance-voucher/get-by-biz', params })
  },

  generateVoucher: async (data: {
    ledgerId: number
    bizType: number
    bizId: number
    templateId: number
    voucherTime?: string
    remark?: string
  }) => {
    return await request.post({ url: '/erp/finance-voucher/generate', data })
  },

  approveVoucher: async (ids: number[]) => {
    return await request.post({ url: '/erp/finance-voucher/approve', data: { ids } })
  },

  cancelApproveVoucher: async (ids: number[]) => {
    return await request.post({ url: '/erp/finance-voucher/cancel-approve', data: { ids } })
  },

  postVoucher: async (ids: number[]) => {
    return await request.post({ url: '/erp/finance-voucher/post', data: { ids } })
  },

  cancelPostVoucher: async (ids: number[]) => {
    return await request.post({ url: '/erp/finance-voucher/cancel-post', data: { ids } })
  },

  reverseVoucher: async (data: { id: number; voucherTime?: string; remark?: string }) => {
    return await request.post({ url: '/erp/finance-voucher/reverse', data })
  },

  recomputeVoucher: async (data: { bizType: number; bizId: number; remark?: string }) => {
    return await request.post({ url: '/erp/finance-voucher/recompute', data })
  }
}
