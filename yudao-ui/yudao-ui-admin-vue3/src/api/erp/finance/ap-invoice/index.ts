import request from '@/config/axios'

export interface ErpApInvoiceMatchItemVO {
  id?: number
  apStatementId?: number
  sourceOrderId?: number
  sourceOrderNo?: string
  sourcePurchaseInId?: number
  sourcePurchaseInNo?: string
  sourcePurchaseInItemId?: number
  productId?: number
  productName?: string
  matchCount?: number
  matchAmount?: number
  status?: number
  statusName?: string
  remark?: string
  createTime?: string
}

export interface ErpApInvoiceVO {
  id?: number
  supplierId?: number
  supplierName?: string
  invoiceNo?: string
  invoiceDate?: string
  invoiceType?: number
  invoiceTypeName?: string
  totalCount?: number
  matchedCount?: number
  totalAmount?: number
  matchedAmount?: number
  unmatchedAmount?: number
  toleranceAmount?: number
  differenceAmount?: number
  matchStatus?: number
  matchStatusName?: string
  differenceReason?: string
  remark?: string
  createTime?: string
  items?: ErpApInvoiceMatchItemVO[]
}

export interface ErpApInvoicePageReqVO {
  pageNo?: number
  pageSize?: number
  invoiceNo?: string
  supplierId?: number
  invoiceType?: number
  matchStatus?: number
  invoiceDate?: string[]
  remark?: string
}

export interface ErpApInvoiceSaveReqVO {
  id?: number
  supplierId?: number
  invoiceNo?: string
  invoiceDate?: string
  invoiceType?: number
  totalCount?: number
  totalAmount?: number
  toleranceAmount?: number
  differenceReason?: string
  remark?: string
}

export interface ErpApInvoicePendingItemVO {
  apStatementId?: number
  sourceOrderId?: number
  sourceOrderNo?: string
  sourcePurchaseInId?: number
  sourcePurchaseInNo?: string
  sourcePurchaseInItemId?: number
  supplierId?: number
  productId?: number
  productName?: string
  bizDate?: string
  totalCount?: number
  matchedCount?: number
  remainCount?: number
  totalAmount?: number
  matchedAmount?: number
  remainAmount?: number
}

export interface ErpApInvoicePendingItemPageReqVO {
  pageNo?: number
  pageSize?: number
  invoiceId?: number
  supplierId?: number
  sourceOrderNo?: string
  purchaseInNo?: string
}

export interface ErpApInvoiceConfirmMatchItemReqVO {
  purchaseInItemId: number
  matchCount: number
  matchAmount: number
  remark?: string
}

export interface ErpApInvoiceConfirmMatchReqVO {
  invoiceId: number
  differenceReason?: string
  items: ErpApInvoiceConfirmMatchItemReqVO[]
}

export interface ErpApInvoiceCancelMatchReqVO {
  ids: number[]
}

export const ERP_AP_INVOICE_TYPE_OPTIONS = [
  { label: '专票', value: 10 },
  { label: '普票', value: 20 },
  { label: '电子票', value: 30 }
]

export const ERP_AP_INVOICE_MATCH_STATUS_OPTIONS = [
  { label: '未匹配', value: 10 },
  { label: '部分匹配', value: 20 },
  { label: '已匹配', value: 30 },
  { label: '异常', value: 40 }
]

export const ERP_AP_INVOICE_MATCH_ITEM_STATUS_OPTIONS = [
  { label: '生效', value: 10 },
  { label: '已撤销', value: 20 }
]

export const ApInvoiceApi = {
  getApInvoicePage: async (params: ErpApInvoicePageReqVO) => {
    return await request.get({ url: '/erp/ap-invoice/page', params })
  },

  getApInvoice: async (id: number) => {
    return await request.get({ url: `/erp/ap-invoice/get?id=${id}` })
  },

  createApInvoice: async (data: ErpApInvoiceSaveReqVO) => {
    return await request.post({ url: '/erp/ap-invoice/create', data })
  },

  updateApInvoice: async (data: ErpApInvoiceSaveReqVO) => {
    return await request.put({ url: '/erp/ap-invoice/update', data })
  },

  getPendingItemPage: async (params: ErpApInvoicePendingItemPageReqVO) => {
    return await request.get({ url: '/erp/ap-invoice/pending-item-page', params })
  },

  confirmMatch: async (data: ErpApInvoiceConfirmMatchReqVO) => {
    return await request.post({ url: '/erp/ap-invoice/confirm-match', data })
  },

  cancelMatch: async (data: ErpApInvoiceCancelMatchReqVO) => {
    return await request.post({ url: '/erp/ap-invoice/cancel-match', data })
  }
}
