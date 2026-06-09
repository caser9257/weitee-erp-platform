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
}

export interface MarketLedgerVO {
  projectId: number
  projectNo: string
  projectName: string
  lifecycleStage: string
  currentBlocker?: string
  currentPendingRole?: string
  contractId?: number
  contractNo?: string
  contractName?: string
  contractAuditStatus?: number
  shipmentReleaseRule?: string
  invoiceTrigger?: string
  collectionRule?: string
  orderId: number
  orderNo: string
  orderStatus: number
  orderTotalPrice: number
  deliveryDate?: string | Date
  shipmentReleaseStatus?: string
  shipmentReleaseReason?: string
  receivedAmount?: number
  receivableAmount?: number
  receiptProgress?: number
  shippedCount?: number
  orderCount?: number
  shipmentProgress?: number
  invoicedAmount?: number
  invoiceStatus?: string
  acceptanceStatus?: string
}

export interface MarketLedgerStatsVO {
  totalOrderCount: number
  totalOrderAmount: number
  totalReceivedAmount: number
  pendingReleaseCount: number
  shippedOrderCount: number
  pendingInvoiceCount: number
  pendingAcceptanceCount: number
  abnormalOrderCount: number
}

export const MarketLedgerApi = {
  // 分页查询市场执行台账
  getLedgerPage: async (params: MarketLedgerPageReqVO) => {
    return await request.get({ url: '/erp/market-ledger/page', params })
  },

  // 获取市场执行台账统计
  getLedgerStats: async () => {
    return await request.get<MarketLedgerStatsVO>({ url: '/erp/market-ledger/stats' })
  },

  // 获取项目级聚合视图
  getProjectSummary: async (projectId: number) => {
    return await request.get<MarketLedgerVO>({ url: `/erp/market-ledger/project/${projectId}` })
  }
}
