import request from '@/config/axios'

export interface ShipmentReleasePageReqVO {
  pageNo: number
  pageSize: number
  orderNo?: string
  projectNo?: string
  contractNo?: string
  customerId?: number
  saleUserId?: number
  releaseStatus?: string
  deliveryDateStart?: string
  deliveryDateEnd?: string
}

export interface ShipmentReleasePageVO {
  orderId: number
  orderNo?: string
  projectId?: number
  projectNo?: string
  projectName?: string
  customerId?: number
  customerName?: string
  contractId?: number
  contractNo?: string
  contractName?: string
  saleUserId?: number
  saleUserName?: string
  orderTotalPrice?: number | string
  receivedAmount?: number | string
  receivableAmount?: number | string
  releaseStatus?: string
  shipmentReleaseReason?: string
  releaseRule?: string
  invoiceTrigger?: string
  collectionRule?: string
  deliveryDate?: string | Date
  blockerReasons?: string[]
  pendingRole?: string
  createTime?: string | Date
}

export interface ShipmentReleaseStatsVO {
  totalCount?: number
  blockedCount?: number
  financeReviewCount?: number
  releasedCount?: number
}

export interface ShipmentReleaseResultVO {
  releasable?: boolean
  releaseStatus?: string
  blockerReasons?: string[]
  pendingRole?: string
  details?: Array<{
    checkItem?: string
    passed?: boolean
    message?: string
  }>
}

export const ShipmentReleaseApi = {
  getShipmentReleasePage: async (params: ShipmentReleasePageReqVO) => {
    return await request.get<{ list: ShipmentReleasePageVO[]; total: number }>({
      url: '/erp/shipment-release/page',
      params
    })
  },

  getShipmentReleaseStats: async () => {
    return await request.get<ShipmentReleaseStatsVO>({ url: '/erp/shipment-release/stats' })
  },

  checkShipmentRelease: async (data: { orderId: number }) => {
    return await request.post<ShipmentReleaseResultVO>({ url: '/erp/shipment-release/check', data })
  },

  submitFinanceApproval: async (orderId: number) => {
    return await request.post<boolean>({
      url: '/erp/shipment-release/submit-finance',
      params: { orderId }
    })
  },

  approveFinance: async (orderId: number, remark?: string) => {
    return await request.post<boolean>({
      url: '/erp/shipment-release/approve',
      params: { orderId, remark }
    })
  },

  rejectFinance: async (orderId: number, reason: string) => {
    return await request.post<boolean>({
      url: '/erp/shipment-release/reject',
      params: { orderId, reason }
    })
  },

  createSaleOutFromRelease: async (orderId: number, warehouseId: number) => {
    return await request.post<number>({
      url: '/erp/shipment-release/create-sale-out',
      params: { orderId, warehouseId }
    })
  }
}
