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
  prepaymentTime?: Date[]
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
    return await request.get({ url: `/erp/prepayment/page`, params })
  },
  getFinancePrepaymentTrace: async (id: number) => {
    return await request.get({ url: `/erp/prepayment/trace?id=${id}` })
  },
  deleteFinancePrepayment: async (ids: number[]) => {
    return await request.delete({ url: `/erp/prepayment/delete`, params: { ids: ids.join(',') } })
  },
  updateFinancePrepaymentStatus: async (id: number, status: number) => {
    return await request.put({ url: `/erp/prepayment/update-status`, data: { id, status } })
  },
  rollbackFinancePrepaymentAllocate: async (data: { ids: number[] }) => {
    return await request.put({ url: `/erp/prepayment/rollback-allocate`, data })
  }
}
