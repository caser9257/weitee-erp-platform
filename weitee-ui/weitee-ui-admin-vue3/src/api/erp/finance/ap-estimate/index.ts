import request from '@/config/axios'

export interface ErpApEstimateItemVO {
  id?: number
  sourcePurchaseInItemId?: number
  sourceOrderItemId?: number
  sourceOrderNo?: string
  productId?: number
  productName?: string
  productBarCode?: string
  productUnitName?: string
  warehouseId?: number
  warehouseName?: string
  projectId?: number
  projectName?: string
  count?: number
  sourceAmount?: number
  taxAmount?: number
  amount?: number
  remark?: string
}

export interface ErpApEstimateVO {
  id?: number
  estimateNo?: string
  estimateMonth?: string
  sourcePurchaseInId?: number
  sourcePurchaseInNo?: string
  sourceOrderId?: number
  sourceOrderNo?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  accountName?: string
  sourceAmount?: number
  amount?: number
  status?: number
  statusName?: string
  confirmUserId?: number
  confirmTime?: string
  reverseUserId?: number
  reverseTime?: string
  reverseRemark?: string
  remark?: string
  creator?: string
  creatorName?: string
  createTime?: string
  items?: ErpApEstimateItemVO[]
}

export interface ErpApEstimatePageReqVO {
  pageNo?: number
  pageSize?: number
  estimateNo?: string
  estimateMonth?: string
  sourcePurchaseInNo?: string
  supplierId?: number
  status?: number
}

export interface ErpApEstimateScanReqVO {
  estimateMonth: string
}

export interface ErpApEstimateActionReqVO {
  ids: number[]
  remark?: string
}

export const ERP_AP_ESTIMATE_STATUS_OPTIONS = [
  { label: '待确认', value: 10 },
  { label: '已确认', value: 20 },
  { label: '已冲回', value: 30 }
]

export const ApEstimateApi = {
  getApEstimatePage: async (params: ErpApEstimatePageReqVO) => {
    return await request.get({ url: '/erp/ap-estimate/page', params })
  },

  getApEstimate: async (id: number) => {
    return await request.get({ url: `/erp/ap-estimate/get?id=${id}` })
  },

  scanMonth: async (data: ErpApEstimateScanReqVO) => {
    return await request.post({ url: '/erp/ap-estimate/scan-month', data })
  },

  confirm: async (data: ErpApEstimateActionReqVO) => {
    return await request.post({ url: '/erp/ap-estimate/confirm', data })
  },

  reverse: async (data: ErpApEstimateActionReqVO) => {
    return await request.post({ url: '/erp/ap-estimate/reverse', data })
  },

  exportApEstimate: async (params: ErpApEstimatePageReqVO) => {
    return await request.download({ url: '/erp/ap-estimate/export-excel', params })
  }
}
