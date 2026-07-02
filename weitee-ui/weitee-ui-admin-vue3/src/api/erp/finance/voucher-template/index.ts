import request from '@/config/axios'

export interface ErpFinanceVoucherTemplateVO {
  id?: number
  ledgerId?: number
  ledgerName?: string
  bizType?: number
  bizTypeName?: string
  name?: string
  status?: number
  autoGenerate?: boolean
  defaultSummary?: string
  remark?: string
  researchCategory?: number
  researchCategoryName?: string
  researchTemplate?: boolean
  createTime?: string
  items?: ErpFinanceVoucherTemplateItemVO[]
}

export interface ErpFinanceVoucherTemplateItemVO {
  id?: number
  entryNo?: number
  entryDirection?: number
  entryDirectionName?: string
  subjectCode?: string
  subjectName?: string
  amountSource?: number
  amountSourceName?: string
  amountSourceValue?: number | string
  summary?: string
}

export interface ErpFinanceVoucherTemplatePageReqVO {
  pageNo: number
  pageSize: number
  ledgerId?: number
  bizType?: number
  name?: string
  status?: number
  autoGenerate?: boolean
}

export interface ErpFinanceVoucherTemplateSaveReqVO {
  id?: number
  ledgerId: number
  bizType: number
  name: string
  status: number
  autoGenerate?: boolean
  defaultSummary?: string
  remark?: string
  researchCategory?: number
  researchTemplate?: boolean
  items: ErpFinanceVoucherTemplateItemVO[]
}

export const FinanceVoucherTemplateApi = {
  getVoucherTemplatePage: async (params: ErpFinanceVoucherTemplatePageReqVO) => {
    return await request.get<{ list: ErpFinanceVoucherTemplateVO[]; total: number }>({
      url: '/erp/finance-voucher-template/page',
      params
    })
  },

  getVoucherTemplate: async (id: number) => {
    return await request.get<ErpFinanceVoucherTemplateVO>({
      url: '/erp/finance-voucher-template/get',
      params: { id }
    })
  },

  getVoucherTemplateSimpleList: async (params: { ledgerId: number; bizType: number }) => {
    return await request.get<ErpFinanceVoucherTemplateVO[]>({
      url: '/erp/finance-voucher-template/simple-list',
      params
    })
  },

  createVoucherTemplate: async (data: ErpFinanceVoucherTemplateSaveReqVO) => {
    return await request.post<number>({ url: '/erp/finance-voucher-template/create', data })
  },

  updateVoucherTemplate: async (data: ErpFinanceVoucherTemplateSaveReqVO) => {
    return await request.put<boolean>({ url: '/erp/finance-voucher-template/update', data })
  },

  deleteVoucherTemplate: async (id: number) => {
    return await request.delete<boolean>({
      url: '/erp/finance-voucher-template/delete',
      params: { id }
    })
  }
}
