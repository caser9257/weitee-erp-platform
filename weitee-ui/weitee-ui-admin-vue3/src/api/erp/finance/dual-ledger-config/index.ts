import request from '@/config/axios'

export interface ErpFinanceDualLedgerConfigVO {
  id?: number
  bizType?: number
  bizTypeName?: string
  externalLedgerId?: number
  externalLedgerName?: string
  internalLedgerId?: number
  internalLedgerName?: string
  status?: number
  remark?: string
}

export interface ErpFinanceDualLedgerConfigSaveReqVO {
  id?: number
  bizType?: number
  externalLedgerId?: number
  internalLedgerId?: number
  status?: number
  remark?: string
}

export interface ErpFinanceDualLedgerConfigPageReqVO {
  pageNo?: number
  pageSize?: number
  bizType?: number
  externalLedgerId?: number
  internalLedgerId?: number
  status?: number
  remark?: string
}

export const FinanceDualLedgerConfigApi = {
  getDualLedgerConfigPage: async (params: ErpFinanceDualLedgerConfigPageReqVO) => {
    return await request.get({ url: '/erp/finance-dual-ledger-config/page', params })
  },
  getDualLedgerConfig: async (id: number) => {
    return await request.get({ url: `/erp/finance-dual-ledger-config/get?id=${id}` })
  },
  createDualLedgerConfig: async (data: ErpFinanceDualLedgerConfigSaveReqVO) => {
    return await request.post({ url: '/erp/finance-dual-ledger-config/create', data })
  },
  updateDualLedgerConfig: async (data: ErpFinanceDualLedgerConfigSaveReqVO) => {
    return await request.put({ url: '/erp/finance-dual-ledger-config/update', data })
  },
  deleteDualLedgerConfig: async (id: number) => {
    return await request.delete({ url: `/erp/finance-dual-ledger-config/delete?id=${id}` })
  },
  getDualLedgerConfigSimpleList: async () => {
    return await request.get({ url: '/erp/finance-dual-ledger-config/simple-list' })
  }
}
