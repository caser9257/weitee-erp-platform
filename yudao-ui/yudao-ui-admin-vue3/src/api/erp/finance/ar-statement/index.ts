import request from '@/config/axios'

export interface ArStatementPageReqVO {
  pageNo: number
  pageSize: number
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
  createTime?: Date | string
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
  bizDate?: string
  dueDate?: string
  invoiceStatus?: number
  invoiceNo?: string
  invoiceAmount?: number
  status?: number
  remark?: string
  createTime?: Date | string
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

// 应收台账状态
export const AR_STATEMENT_STATUS = {
  UNRECEIVED: 0,      // 待收
  PARTIAL_RECEIVED: 1, // 部分收
  SETTLED: 2,          // 已结清
  CLOSED: 3            // 已关闭
} as const

// 应收台账业务类型
export const AR_STATEMENT_BIZ_TYPE = {
  SALE_OUT: 21,     // 销售出库
  SALE_RETURN: 22   // 销售退货
} as const

export const ArStatementApi = {
  // 分页查询
  getPage: async (params: ArStatementPageReqVO) => {
    return await request.get({ url: '/erp/ar-statement/page', params })
  },

  // 获取详情
  get: async (id: number) => {
    return await request.get({ url: '/erp/ar-statement/get', params: { id } })
  },

  // 按客户汇总
  getSummary: async (customerId?: number) => {
    return await request.get<ArStatementSummaryVO[]>({
      url: '/erp/ar-statement/summary',
      params: { customerId }
    })
  },

  // 按订单汇总
  getSummaryByOrder: async (orderId: number) => {
    return await request.get<ArStatementSummaryVO>({
      url: '/erp/ar-statement/summary-by-order',
      params: { orderId }
    })
  }
}
