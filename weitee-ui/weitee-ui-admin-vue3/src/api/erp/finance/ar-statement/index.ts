import request from '@/config/axios'

export interface ArStatementPageReqVO {
  pageNo?: number
  pageSize?: number
  statementNo?: string
  bizType?: number
  bizNo?: string
  customerId?: number
  sourceOrderId?: number
  accountId?: number
  currencyCode?: string
  invoiceStatus?: number
  status?: number
  bizDate?: string[]
  dueDate?: string[]
}

export interface ArStatementItemVO {
  id?: number
  itemType?: number
  refType?: number
  refId?: number
  refNo?: string
  amount?: number
  afterReceivedAmount?: number
  afterRemainAmount?: number
  remark?: string
  createTime?: string
}

export interface ArStatementVO {
  id?: number
  statementNo?: string
  bizType?: number
  bizId?: number
  bizNo?: string
  sourceOrderId?: number
  sourceOrderNo?: string
  customerId?: number
  customerName?: string
  accountId?: number
  accountName?: string
  amount?: number
  receivedAmount?: number
  remainAmount?: number
  currencyCode?: string
  bizDate?: string | number[]
  dueDate?: string | number[]
  invoiceStatus?: number
  invoiceNo?: string
  invoiceAmount?: number
  status?: number
  remark?: string
  createTime?: string
  items?: ArStatementItemVO[]
}

export interface ArStatementSummaryVO {
  customerId?: number
  customerName?: string
  totalAmount?: number
  totalReceivedAmount?: number
  totalRemainAmount?: number
  statementCount?: number
}

export const AR_STATEMENT_STATUS_OPTIONS = [
  { label: '待收', value: 0 },
  { label: '部分收', value: 1 },
  { label: '已结清', value: 2 },
  { label: '已关闭', value: 3 }
]

export const AR_STATEMENT_BIZ_TYPE_OPTIONS = [
  { label: '销售出库', value: 21 },
  { label: '销售退货', value: 22 }
]

export const AR_STATEMENT_INVOICE_STATUS_OPTIONS = [
  { label: '未开票', value: 0 },
  { label: '部分开票', value: 1 },
  { label: '已开票', value: 2 }
]

export const AR_STATEMENT_ITEM_TYPE_OPTIONS = [
  { label: '应收', value: 1 },
  { label: '收款分配', value: 2 },
  { label: '收款退回', value: 3 },
  { label: '台账关闭', value: 4 }
]

export const ArStatementApi = {
  getPage: (params: ArStatementPageReqVO) =>
    request.get<{ list: ArStatementVO[]; total: number }>({ url: '/erp/ar-statement/page', params }),
  get: (id: number) => request.get<ArStatementVO>({ url: '/erp/ar-statement/get', params: { id } }),
  getSummary: (customerId?: number) =>
    request.get<ArStatementSummaryVO[]>({
      url: '/erp/ar-statement/summary',
      params: customerId == null ? undefined : { customerId }
    }),
  exportExcel: (params: ArStatementPageReqVO) =>
    request.download({ url: '/erp/ar-statement/export-excel', params })
}
