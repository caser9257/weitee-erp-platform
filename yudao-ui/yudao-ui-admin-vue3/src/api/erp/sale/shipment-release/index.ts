import request from '@/config/axios'

export interface ShipmentReleaseCheckReqVO {
  orderId: number
}

export interface ShipmentReleaseDetailVO {
  checkItem: string
  passed: boolean
  message: string
}

export interface ShipmentReleaseResultVO {
  releasable: boolean
  releaseStatus: string
  blockerReasons: string[]
  pendingRole?: string
  details: ShipmentReleaseDetailVO[]
}

export interface ShipmentReleasePageReqVO {
  pageNo: number
  pageSize: number
  orderNo?: string
  projectNo?: string
  contractNo?: string
  customerId?: number
  saleUserId?: number
  releaseStatus?: string
}

export interface ShipmentReleasePageVO {
  orderId: number
  orderNo: string
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
  orderTotalPrice?: number
  receivedAmount?: number
  receivableAmount?: number
  releaseStatus?: string
  shipmentReleaseReason?: string
  releaseRule?: string
  invoiceTrigger?: string
  collectionRule?: string
  deliveryDate?: string | Date
  blockerReasons?: string[]
  pendingRole?: string
  financeApprovalRequired?: number
}

export interface ShipmentReleaseStatsVO {
  totalCount: number
  blockedCount: number
  financeReviewCount: number
  releasedCount: number
}

export const ShipmentReleaseApi = {
  getShipmentReleasePage: async (params: ShipmentReleasePageReqVO) => {
    return await request.get({ url: `/erp/shipment-release/page`, params })
  },

  getShipmentReleaseStats: async () => {
    return await request.get<ShipmentReleaseStatsVO>({ url: `/erp/shipment-release/stats` })
  },

  checkShipmentRelease: async (data: ShipmentReleaseCheckReqVO) => {
    return await request.post<ShipmentReleaseResultVO>({ url: `/erp/shipment-release/check`, data })
  },

  submitFinanceApproval: async (orderId: number, approverId: number) => {
    return await request.post({ url: `/erp/shipment-release/submit-finance`, params: { orderId, approverId } })
  },

  approveFinance: async (orderId: number, approverId: number, remark?: string) => {
    return await request.post({ url: `/erp/shipment-release/approve`, params: { orderId, approverId, remark } })
  },

  rejectFinance: async (orderId: number, approverId: number, reason: string) => {
    return await request.post({ url: `/erp/shipment-release/reject`, params: { orderId, approverId, reason } })
  },

  // 从放行创建出库单
  createSaleOutFromRelease: async (orderId: number, warehouseId: number) => {
    return await request.post<number>({
      url: `/erp/shipment-release/create-sale-out`,
      params: { orderId, warehouseId }
    })
  }
}
