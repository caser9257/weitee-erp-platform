import request from '@/config/axios'

export interface ErpFinanceSubjectVO {
  id?: number
  ledgerId?: number
  ledgerName?: string
  parentId?: number
  subjectCode?: string
  subjectName?: string
  subjectType?: number
  subjectTypeName?: string
  balanceDirection?: number
  balanceDirectionName?: string
  leaf?: boolean
  status?: number
  sort?: number
  remark?: string
  createTime?: string
}

export interface ErpFinanceSubjectSaveReqVO {
  id?: number
  ledgerId?: number
  parentId?: number
  subjectCode?: string
  subjectName?: string
  subjectType?: number
  balanceDirection?: number
  leaf?: boolean
  status?: number
  sort?: number
  remark?: string
}

export interface ErpFinanceSubjectPageReqVO {
  pageNo?: number
  pageSize?: number
  ledgerId?: number
  parentId?: number
  subjectCode?: string
  subjectName?: string
  subjectType?: number
  status?: number
}

export const FinanceSubjectApi = {
  getSubjectSimpleList: async (params?: { ledgerId?: number; status?: number }) => {
    return await request.get({ url: '/erp/finance-subject/simple-list', params })
  },

  getSubjectPage: async (params: ErpFinanceSubjectPageReqVO) => {
    return await request.get({ url: '/erp/finance-subject/page', params })
  },

  getSubject: async (id: number) => {
    return await request.get({ url: `/erp/finance-subject/get?id=${id}` })
  },

  createSubject: async (data: ErpFinanceSubjectSaveReqVO) => {
    return await request.post({ url: '/erp/finance-subject/create', data })
  },

  updateSubject: async (data: ErpFinanceSubjectSaveReqVO) => {
    return await request.put({ url: '/erp/finance-subject/update', data })
  },

  deleteSubject: async (id: number) => {
    return await request.delete({ url: `/erp/finance-subject/delete?id=${id}` })
  }
}
