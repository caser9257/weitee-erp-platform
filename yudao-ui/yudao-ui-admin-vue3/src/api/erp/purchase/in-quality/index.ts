import request from '@/config/axios'

export const PURCHASE_IN_QUALITY_STATUS = {
  FIRST_CHECKING: 20,
  WAIT_RECHECK: 30,
  RECHECKING: 40,
  DONE: 50,
  VOID: 60
} as const

export const PURCHASE_IN_QUALITY_RESULT = {
  TO_DECIDE: 10,
  PARTIAL: 20,
  PASSED: 30,
  REJECTED: 40
} as const

export const PURCHASE_IN_QUALITY_ROUND_TYPE = {
  FIRST: 1,
  RECHECK: 2
} as const

export const QC_SAMPLING_SCHEME_TYPE = {
  FULL: 10,
  RATIO: 20,
  FIXED: 30,
  SKIP: 40
} as const

// 自定义不良原因使用固定占位 ID，避免变更既有提交流程
export const CUSTOM_DEFECT_REASON_ID = -1

export interface QcDefectReasonSimpleVO {
  id: number
  code?: string
  name: string
}

export interface PurchaseInQualityDefectVO {
  id?: number
  roundId?: number
  qualityItemId?: number
  defectReasonId?: number
  defectReasonName?: string
  defectCount?: number
  defectRemark?: string
}

export interface PurchaseInQualityRoundVO {
  id?: number
  qualityItemId?: number
  roundNo?: number
  roundType?: number
  sampleCount?: number
  passCount?: number
  rejectCount?: number
  result?: number
  checkerUserId?: number
  checkerUserNickname?: string
  checkTime?: Date | string | number
  remark?: string
}

export interface PurchaseInQualityItemVO {
  id?: number
  purchaseInItemId?: number
  productId?: number
  warehouseId?: number
  count?: number
  sampleCount?: number
  qaPassCount?: number
  qaRejectCount?: number
  qaResult?: number
  qaRemark?: string
  productName?: string
  productBarCode?: string
  productUnitName?: string
}

export interface PurchaseInQualityVO {
  id?: number
  no?: string
  purchaseInId?: number
  purchaseInNo?: string
  status?: number
  result?: number
  currentRoundNo?: number
  recheckRequired?: boolean
  recheckReason?: string
  recheckApplyUserId?: number
  recheckApplyUserNickname?: string
  recheckApplyTime?: Date | string | number
  samplingSchemeId?: number
  samplingSchemeName?: string
  samplingSchemeType?: number
  samplingRatio?: number
  samplingFixedCount?: number
  minSampleCount?: number
  maxSampleCount?: number
  assignedCheckerUserId?: number
  assignedCheckerUserNickname?: string
  assignedCheckerTime?: Date | string | number
  checkerUserId?: number
  checkerUserNickname?: string
  checkTime?: Date | string | number
  remark?: string
  passCount?: number
  rejectCount?: number
  purchaseInStatus?: number
  qaStatus?: number
  stockInStatus?: number
  stockInCount?: number
  remainingStockInCount?: number
  orderNo?: string
  supplierName?: string
  items?: PurchaseInQualityItemVO[]
  rounds?: PurchaseInQualityRoundVO[]
  defects?: PurchaseInQualityDefectVO[]
}

export interface PurchaseInQualityCreateReqVO {
  purchaseInId: number
}

export interface PurchaseInQualityAssignCheckerReqVO {
  id: number
  assignedCheckerUserId: number
}

export interface PurchaseInQualityDefectReqVO {
  defectReasonId: number
  defectReasonName?: string
  defectCount: number
  defectRemark?: string
}

export interface PurchaseInQualitySubmitFirstCheckItemReqVO {
  qualityItemId: number
  sampleCount: number
  roundPassCount: number
  roundRejectCount: number
  roundRemark?: string
  defects?: PurchaseInQualityDefectReqVO[]
}

export interface PurchaseInQualitySubmitFirstCheckReqVO {
  id: number
  remark?: string
  items: PurchaseInQualitySubmitFirstCheckItemReqVO[]
}

export interface PurchaseInQualityStartRecheckReqVO {
  id: number
  recheckReason: string
}

export interface PurchaseInQualitySubmitRecheckItemReqVO {
  qualityItemId: number
  sampleCount: number
  roundPassCount: number
  roundRejectCount: number
  finalPassCount: number
  finalRejectCount: number
  roundRemark?: string
  defects?: PurchaseInQualityDefectReqVO[]
}

export interface PurchaseInQualitySubmitRecheckReqVO {
  id: number
  remark?: string
  items: PurchaseInQualitySubmitRecheckItemReqVO[]
}

export const PurchaseInQualityApi = {
  getPurchaseInQualityPage: async (params: any) => {
    return await request.get({ url: `/erp/purchase-in-quality/page`, params })
  },

  getPurchaseInQuality: async (id: number) => {
    return await request.get({ url: `/erp/purchase-in-quality/get?id=` + id })
  },

  getPurchaseInQualityByPurchaseInId: async (purchaseInId: number) => {
    return await request.get({
      url: `/erp/purchase-in-quality/get-by-purchase-in-id?purchaseInId=` + purchaseInId
    })
  },

  getDefectReasonSimpleList: async () => {
    return await request.get({ url: `/erp/purchase-in-quality/defect-reason-simple-list` })
  },

  createPurchaseInQuality: async (data: PurchaseInQualityCreateReqVO) => {
    return await request.post({ url: `/erp/purchase-in-quality/create`, data })
  },

  assignChecker: async (data: PurchaseInQualityAssignCheckerReqVO) => {
    return await request.post({ url: `/erp/purchase-in-quality/assign-checker`, data })
  },

  submitFirstCheck: async (data: PurchaseInQualitySubmitFirstCheckReqVO) => {
    return await request.post({ url: `/erp/purchase-in-quality/submit-first-check`, data })
  },

  startRecheck: async (data: PurchaseInQualityStartRecheckReqVO) => {
    return await request.post({ url: `/erp/purchase-in-quality/start-recheck`, data })
  },

  submitRecheck: async (data: PurchaseInQualitySubmitRecheckReqVO) => {
    return await request.post({ url: `/erp/purchase-in-quality/submit-recheck`, data })
  }
}
