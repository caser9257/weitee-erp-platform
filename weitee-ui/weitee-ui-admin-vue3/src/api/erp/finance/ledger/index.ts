import request from '@/config/axios'

export interface ErpFinanceLedgerVO {
  id?: number
  no?: string
  name?: string
  status?: number
  sort?: number
  defaultStatus?: boolean
  remark?: string
  createTime?: string
}

export interface ErpFinanceLedgerSaveReqVO {
  id?: number
  no?: string
  name?: string
  status?: number
  sort?: number
  defaultStatus?: boolean
  remark?: string
}

export interface ErpFinanceLedgerPageReqVO {
  pageNo?: number
  pageSize?: number
  no?: string
  name?: string
  status?: number
  defaultStatus?: boolean
  remark?: string
}

export const FinanceLedgerApi = {
  getLedgerSimpleList: async () => {
    return await request.get({ url: '/erp/finance-ledger/simple-list' })
  },

  getLedgerPage: async (params: ErpFinanceLedgerPageReqVO) => {
    return await request.get({ url: '/erp/finance-ledger/page', params })
  },

  getLedger: async (id: number) => {
    return await request.get({ url: `/erp/finance-ledger/get?id=${id}` })
  },

  createLedger: async (data: ErpFinanceLedgerSaveReqVO) => {
    return await request.post({ url: '/erp/finance-ledger/create', data })
  },

  updateLedger: async (data: ErpFinanceLedgerSaveReqVO) => {
    return await request.put({ url: '/erp/finance-ledger/update', data })
  },

  updateLedgerDefaultStatus: async (id: number, defaultStatus: boolean) => {
    return await request.put({
      url: '/erp/finance-ledger/update-default-status',
      params: { id, defaultStatus }
    })
  },

  deleteLedger: async (id: number) => {
    return await request.delete({ url: `/erp/finance-ledger/delete?id=${id}` })
  },

  exportLedger: async (params: ErpFinanceLedgerPageReqVO) => {
    return await request.download({ url: '/erp/finance-ledger/export-excel', params })
  }
}
