import request from '@/config/axios'

export interface ApStatementItemVO {
  id?: number
  itemType?: number
  refType?: number
  refId?: number
  refNo?: string
  amount?: number
  afterPaidAmount?: number
  afterRemainAmount?: number
  remark?: string
  operatorName?: string
  createTime?: string
}

export interface ApStatementVO {
  id?: number
  statementNo?: string
  bizType?: number
  bizId?: number
  bizNo?: string
  sourceOrderId?: number
  sourceOrderNo?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  accountName?: string
  amount?: number
  paidAmount?: number
  remainAmount?: number
  currencyCode?: string
  bizDate?: string
  dueDate?: string
  invoiceStatus?: number
  invoiceNo?: string
  invoiceAmount?: number
  status?: number
  remark?: string
  creator?: string
  createTime?: string
  items?: ApStatementItemVO[]
}

export interface ApStatementSummaryVO {
  supplierId?: number
  supplierName?: string
  statementCount?: number
  totalAmount?: number
  totalPaidAmount?: number
  totalRemainAmount?: number
}

export interface ApStatementPaymentEnableVO {
  id?: number
  statementNo?: string
  bizType?: number
  bizId?: number
  bizNo?: string
  sourceOrderNo?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  accountName?: string
  amount?: number
  paidAmount?: number
  remainAmount?: number
  currencyCode?: string
  bizDate?: string
  dueDate?: string
  status?: number
  remark?: string
}

export interface ApStatementAgingVO {
  supplierId?: number
  supplierName?: string
  amount0To30?: number
  amount31To60?: number
  amount61To90?: number
  amount91Plus?: number
  totalRemainAmount?: number
}

export interface ApStatementReconciliationVO {
  statementId?: number
  statementNo?: string
  bizType?: number
  bizId?: number
  bizNo?: string
  sourceOrderId?: number
  sourceOrderNo?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  accountName?: string
  amount?: number
  paidAmount?: number
  remainAmount?: number
  currencyCode?: string
  bizDate?: string
  dueDate?: string
  invoiceStatus?: number
  status?: number
  remark?: string
  createTime?: string
}

export interface ApStatementPageReqVO {
  pageNo?: number
  pageSize?: number
  statementNo?: string
  bizType?: number
  bizNo?: string
  supplierId?: number
  accountId?: number
  currencyCode?: string
  invoiceStatus?: number
  status?: number
  bizDate?: string[]
  dueDate?: string[]
}

export interface ApStatementPaymentEnablePageReqVO {
  pageNo?: number
  pageSize?: number
  supplierId?: number
  bizType?: number
  statementNo?: string
  bizNo?: string
  accountId?: number
}

export interface ApStatementAgingReqVO {
  supplierId?: number
  accountId?: number
  asOfDate?: string
}

export interface ApStatementReconciliationReqVO {
  pageNo?: number
  pageSize?: number
  supplierId?: number
  bizType?: number
  statementNo?: string
  bizNo?: string
  status?: number
  bizDate?: string[]
}

export interface ApStatementUpdateInvoiceReqVO {
  id: number
  invoiceStatus: number
  invoiceNo?: string
  invoiceAmount?: number
  remark?: string
}

export const ERP_AP_BIZ_TYPE_OPTIONS = [
  { label: '采购入库', value: 11 },
  { label: '采购退货', value: 12 },
  { label: '委外加工费', value: 30 },
  { label: '费用报销', value: 40 }
]

export const ERP_AP_STATEMENT_STATUS_OPTIONS = [
  { label: '未付款', value: 10 },
  { label: '部分付款', value: 20 },
  { label: '已结清', value: 30 },
  { label: '已关闭', value: 40 }
]

export const ERP_AP_INVOICE_STATUS_OPTIONS = [
  { label: '未收票', value: 0 },
  { label: '部分收票', value: 1 },
  { label: '已收票', value: 2 }
]

export const ERP_AP_STATEMENT_ITEM_TYPE_OPTIONS = [
  { label: '生成应付', value: 10 },
  { label: '付款核销', value: 20 },
  { label: '核销回滚', value: 30 },
  { label: '台账关闭', value: 40 },
  { label: '收票登记', value: 50 }
]

export const ApStatementApi = {
  getApStatementPage: async (params: ApStatementPageReqVO) => {
    return await request.get({ url: '/erp/ap-statement/page', params })
  },

  getApStatement: async (id: number) => {
    return await request.get({ url: `/erp/ap-statement/get?id=${id}` })
  },

  getApStatementSummaryList: async (supplierId?: number) => {
    return await request.get({
      url: '/erp/ap-statement/summary',
      params: supplierId ? { supplierId } : undefined
    })
  },

  getPaymentEnablePage: async (params: ApStatementPaymentEnablePageReqVO) => {
    return await request.get({ url: '/erp/ap-statement/payment-enable-page', params })
  },

  getAgingList: async (params: ApStatementAgingReqVO) => {
    return await request.get({ url: '/erp/ap-statement/aging', params })
  },

  getReconciliationPage: async (params: ApStatementReconciliationReqVO) => {
    return await request.get({ url: '/erp/ap-statement/reconciliation', params })
  },

  updateInvoice: async (data: ApStatementUpdateInvoiceReqVO) => {
    return await request.put({ url: '/erp/ap-statement/update-invoice', data })
  },

  exportApStatement: async (params: ApStatementPageReqVO) => {
    return await request.download({ url: '/erp/ap-statement/export-excel', params })
  }
}
