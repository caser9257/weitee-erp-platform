import request from '@/config/axios'

export interface ErpFinanceReportItemSubjectVO {
  id?: number
  subjectCode?: string
  subjectName?: string
  amountRule?: number
  amountRuleName?: string
  amountSign?: number
}

export interface ErpFinanceReportItemVO {
  id?: number
  ledgerId?: number
  ledgerName?: string
  reportType?: number
  reportTypeName?: string
  itemCategory?: number
  itemCategoryName?: string
  itemCode?: string
  itemName?: string
  status?: number
  sort?: number
  remark?: string
  createTime?: string
  subjects?: ErpFinanceReportItemSubjectVO[]
}

export interface ErpFinanceReportItemSaveReqVO {
  id?: number
  ledgerId?: number
  reportType?: number
  itemCategory?: number
  itemCode?: string
  itemName?: string
  status?: number
  sort?: number
  remark?: string
  subjects?: Array<{
    subjectCode?: string
    amountRule?: number
    amountSign?: number
  }>
}

export interface ErpFinanceReportItemPageReqVO {
  pageNo?: number
  pageSize?: number
  ledgerId?: number
  reportType?: number
  itemCategory?: number
  itemCode?: string
  itemName?: string
  status?: number
}

export const FinanceReportItemApi = {
  getReportItemPage: async (params: ErpFinanceReportItemPageReqVO) => {
    return await request.get({ url: '/erp/finance-report-item/page', params })
  },

  getReportItem: async (id: number) => {
    return await request.get({ url: `/erp/finance-report-item/get?id=${id}` })
  },

  createReportItem: async (data: ErpFinanceReportItemSaveReqVO) => {
    return await request.post({ url: '/erp/finance-report-item/create', data })
  },

  updateReportItem: async (data: ErpFinanceReportItemSaveReqVO) => {
    return await request.put({ url: '/erp/finance-report-item/update', data })
  },

  deleteReportItem: async (id: number) => {
    return await request.delete({ url: `/erp/finance-report-item/delete?id=${id}` })
  },

  initStandardTemplate: async (data: { ledgerId: number; overrideExisting?: boolean }) => {
    return await request.post({ url: '/erp/finance-report-item/init-standard-template', data })
  }
}
