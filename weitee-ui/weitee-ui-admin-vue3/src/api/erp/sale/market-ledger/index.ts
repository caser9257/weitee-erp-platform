import request from '@/config/axios'

export interface MarketLedgerPageReqVO {
  pageNo: number
  pageSize: number
  projectNo?: string
  contractNo?: string
  customerName?: string
  saleUserId?: number
  lifecycleStage?: string
  releaseStatus?: string
  invoiceStatus?: string
  acceptanceStatus?: string
  orderMonth?: string
  deliveryDateStart?: string
  deliveryDateEnd?: string
}

export interface MarketLedgerVO {
  projectId?: number
  projectNo?: string
  projectName?: string
  lifecycleStage?: string
  currentBlocker?: string
  currentPendingRole?: string
  contractId?: number
  contractNo?: string
  contractName?: string
  contractAuditStatus?: number
  shipmentReleaseRule?: string
  invoiceTrigger?: string
  collectionRule?: string
  orderId?: number
  orderNo?: string
  orderStatus?: number
  orderTotalPrice?: number | string
  deliveryDate?: string | Date
  shipmentReleaseStatus?: string
  shipmentReleaseReason?: string
  receivedAmount?: number | string
  receivableAmount?: number | string
  receiptProgress?: number | string
  shippedCount?: number | string
  orderCount?: number | string
  shipmentProgress?: number | string
  invoicedAmount?: number | string
  invoiceStatus?: string
  acceptanceStatus?: string
  customerName?: string
  saleUserName?: string
}

export interface MarketLedgerStatsVO {
  totalOrderCount?: number
  totalOrderAmount?: number | string
  totalReceivedAmount?: number | string
  pendingReleaseCount?: number
  shippedOrderCount?: number
  pendingInvoiceCount?: number
  pendingAcceptanceCount?: number
  abnormalOrderCount?: number
}

export const MarketLedgerApi = {
  getLedgerPage: async (params: MarketLedgerPageReqVO) => {
    return await request.get<{ list: MarketLedgerVO[]; total: number }>({
      url: '/erp/market-ledger/page',
      params
    })
  },

  getLedgerStats: async () => {
    return await request.get<MarketLedgerStatsVO>({ url: '/erp/market-ledger/stats' })
  },

  getProjectSummary: async (projectId: number) => {
    return await request.get<MarketLedgerVO>({ url: `/erp/market-ledger/project/${projectId}` })
  }
}
