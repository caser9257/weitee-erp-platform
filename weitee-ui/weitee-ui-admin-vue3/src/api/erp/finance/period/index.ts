import request from '@/config/axios'

export interface ErpFinancePeriodVO {
  id?: number
  ledgerId?: number
  ledgerName?: string
  periodCode?: string
  periodYear?: number
  periodMonth?: number
  startDate?: string
  endDate?: string
  status?: number
  closeTime?: string
  closeUserId?: number
  remark?: string
  createTime?: string
}

export interface ErpFinancePeriodSaveReqVO {
  id?: number
  ledgerId?: number
  periodYear?: number
  periodMonth?: number
  remark?: string
}

export interface ErpFinancePeriodCreateYearReqVO {
  ledgerId?: number
  periodYear?: number
  remark?: string
}

export interface ErpFinancePeriodPageReqVO {
  pageNo?: number
  pageSize?: number
  ledgerId?: number
  periodCode?: string
  periodYear?: number
  periodMonth?: number
  status?: number
}

export const FinancePeriodApi = {
  getPeriodPage: async (params: ErpFinancePeriodPageReqVO) => {
    return await request.get({ url: '/erp/finance-period/page', params })
  },

  getPeriod: async (id: number) => {
    return await request.get({ url: `/erp/finance-period/get?id=${id}` })
  },

  getCurrentOpenPeriod: async (params: { ledgerId: number; bizDate?: string }) => {
    return await request.get({ url: '/erp/finance-period/current-open', params })
  },

  createPeriod: async (data: ErpFinancePeriodSaveReqVO) => {
    return await request.post({ url: '/erp/finance-period/create', data })
  },

  createPeriodsByYear: async (data: ErpFinancePeriodCreateYearReqVO) => {
    return await request.post({ url: '/erp/finance-period/create-year', data })
  },

  closePeriod: async (id: number) => {
    return await request.put({ url: '/erp/finance-period/close', params: { id } })
  },

  reopenPeriod: async (id: number) => {
    return await request.put({ url: '/erp/finance-period/reopen', params: { id } })
  },

  exportPeriod: async (params: ErpFinancePeriodPageReqVO) => {
    return await request.download({ url: '/erp/finance-period/export-excel', params })
  }
}
