import request from '@/config/axios'

export interface ErpFinancePrepaymentVO {
  id?: number
  no?: string
  prepaymentTime?: Date
  supplierName?: string
  supplierId?: number
  accountName?: string
  accountId?: number
  financeUserName?: string
  financeUserId?: number
  creatorName?: string
  creator?: string
  prepaymentPrice?: number
  allocatedPrice?: number
  remainPrice?: number
  status?: number
  remark?: string
}

export interface ErpFinancePrepaymentPageReqVO {
  pageNo?: number
  pageSize?: number
  no?: string
  prepaymentTime?: string[]
  supplierId?: number
  creator?: string
  financeUserId?: number
  accountId?: number
  status?: number
  remark?: string
}

export interface ErpFinancePrepaymentTraceVO {
  prepayment?: ErpFinancePrepaymentVO
  statements?: {
    id?: number
    statementNo?: string
    bizNo?: string
    amount?: number
    remainAmount?: number
  }[]
  allocates?: {
    id?: number
    bizNo?: string
    statusName?: string
    allocateAmount?: number
    remark?: string
    status?: number
  }[]
}

export const FinancePrepaymentApi = {
  getFinancePrepaymentPage: async (params: ErpFinancePrepaymentPageReqVO) => {
    return await request.get({ url: `/erp/finance-prepayment/page`, params })
  },
  getFinancePrepayment: async (id: number) => {
    return await request.get({ url: `/erp/finance-prepayment/get?id=${id}` })
  },
  createFinancePrepayment: async (data: ErpFinancePrepaymentVO) => {
    return await request.post({ url: `/erp/finance-prepayment/create`, data })
  },
  updateFinancePrepayment: async (data: ErpFinancePrepaymentVO) => {
    return await request.put({ url: `/erp/finance-prepayment/update`, data })
  },
  getFinancePrepaymentTrace: async (id: number) => {
    return await request.get({ url: `/erp/finance-prepayment/get-trace?id=${id}` })
  },
  deleteFinancePrepayment: async (ids: number[]) => {
    return await request.delete({ url: `/erp/finance-prepayment/delete`, params: { ids: ids.join(',') } })
  },
  updateFinancePrepaymentStatus: async (id: number, status: number) => {
    return await request.put({ url: `/erp/finance-prepayment/update-status`, params: { id, status } })
  },
  allocateFinancePrepayment: async (data: { prepaymentId: number; items: { apStatementId: number; allocateAmount: number; remark?: string }[] }) => {
    return await request.post({ url: `/erp/finance-prepayment/allocate`, data })
  },
  rollbackFinancePrepaymentAllocate: async (data: { ids: number[] }) => {
    return await request.post({ url: `/erp/finance-prepayment/rollback-allocate`, data })
  }
}
