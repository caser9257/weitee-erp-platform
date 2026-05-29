import request from '@/config/axios'
import type { ApStatementVO } from '@/api/erp/finance/apar'

export interface ErpFinancePrepaymentAllocateItemVO {
  id?: number
  prepaymentId?: number
  apStatementId?: number
  allocateAmount?: number
  supplierId?: number
  bizType?: number
  bizId?: number
  bizNo?: string
  status?: number
  statusName?: string
  remark?: string
  createTime?: string
}

export interface ErpFinancePrepaymentVO {
  id?: number
  no?: string
  status?: number
  prepaymentTime?: Date | string | number
  financeUserId?: number
  financeUserName?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  accountName?: string
  prepaymentPrice?: number
  allocatedPrice?: number
  remainPrice?: number
  remark?: string
  creator?: string
  creatorName?: string
  createTime?: string
  allocates?: ErpFinancePrepaymentAllocateItemVO[]
}

export interface ErpFinancePrepaymentTraceVO {
  prepayment?: ErpFinancePrepaymentVO
  statements?: ApStatementVO[]
  allocates?: ErpFinancePrepaymentAllocateItemVO[]
}

export interface ErpFinancePrepaymentPageReqVO {
  pageNo?: number
  pageSize?: number
  no?: string
  prepaymentTime?: string[]
  supplierId?: number
  creator?: string | number
  financeUserId?: number
  accountId?: number
  status?: number
  remark?: string
}

export interface ErpFinancePrepaymentSaveReqVO {
  id?: number
  prepaymentTime?: Date | string | number
  financeUserId?: number
  supplierId?: number
  accountId?: number
  prepaymentPrice?: number
  remark?: string
}

export interface ErpFinancePrepaymentAllocateReqVO {
  prepaymentId: number
  items: Array<{
    apStatementId: number
    allocateAmount: number
    remark?: string
  }>
}

export interface ErpFinancePrepaymentRollbackReqVO {
  ids: number[]
  remark?: string
}

export const FinancePrepaymentApi = {
  getFinancePrepaymentPage: async (params: ErpFinancePrepaymentPageReqVO) => {
    return await request.get({ url: '/erp/finance-prepayment/page', params })
  },

  getFinancePrepayment: async (id: number) => {
    return await request.get({ url: `/erp/finance-prepayment/get?id=${id}` })
  },

  getFinancePrepaymentTrace: async (id: number) => {
    return await request.get({ url: `/erp/finance-prepayment/get-trace?id=${id}` })
  },

  createFinancePrepayment: async (data: ErpFinancePrepaymentSaveReqVO) => {
    return await request.post({ url: '/erp/finance-prepayment/create', data })
  },

  updateFinancePrepayment: async (data: ErpFinancePrepaymentSaveReqVO) => {
    return await request.put({ url: '/erp/finance-prepayment/update', data })
  },

  updateFinancePrepaymentStatus: async (id: number, status: number) => {
    return await request.put({
      url: '/erp/finance-prepayment/update-status',
      params: { id, status }
    })
  },

  deleteFinancePrepayment: async (ids: number[]) => {
    return await request.delete({
      url: '/erp/finance-prepayment/delete',
      params: {
        ids: ids.join(',')
      }
    })
  },

  allocateFinancePrepayment: async (data: ErpFinancePrepaymentAllocateReqVO) => {
    return await request.post({
      url: '/erp/finance-prepayment/allocate',
      data
    })
  },

  rollbackFinancePrepaymentAllocate: async (data: ErpFinancePrepaymentRollbackReqVO) => {
    return await request.post({
      url: '/erp/finance-prepayment/rollback-allocate',
      data
    })
  }
}
