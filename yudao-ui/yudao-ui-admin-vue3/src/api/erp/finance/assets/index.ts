import request from '@/config/axios'

export interface FinanceAssetVO {
  id?: number
  no?: string
  name: string
  categoryName: string
  candidateId?: number
  sourceType?: number
  sourceBizId?: number
  sourceBizNo?: string
  sourceItemId?: number
  deptId?: number
  responsibleUserId?: number
  purchaseDate?: string
  startUseDate?: string
  originalAmount: number
  salvageRate: number
  salvageAmount?: number
  depreciationMethod: string
  depreciationPeriodMonths: number
  depreciationStartPeriod: string
  depreciatedAmount?: number
  currentAmount?: number
  status?: number
  lastDepreciationPeriod?: string
  remark?: string
  assetType?: number
  subCategory?: string
  createTime?: string
}

export interface FinanceAssetCandidateVO {
  id: number
  sourceType?: number
  sourceBizId?: number
  sourceBizNo?: string
  assetName: string
  categoryName?: string
  amount?: number
  purchaseDate?: string
  deptId?: number
  responsibleUserId?: number
  status: number
  remark?: string
}

export interface FinanceAssetDepreciationVO {
  id: number
  assetId: number
  assetNo: string
  period: string
  depreciationAmount: number
  beforeDepreciatedAmount: number
  afterDepreciatedAmount: number
  beforeCurrentAmount: number
  afterCurrentAmount: number
  status: number
  remark?: string
}

export interface FinanceAssetTraceVO {
  asset?: FinanceAssetVO
  candidate?: FinanceAssetCandidateVO | null
  purchaseIn?: {
    id?: number
    no?: string
    status?: number
    supplierId?: number
    supplierName?: string
    accountId?: number
    inTime?: string
    totalCount?: number
    totalPrice?: number
    paymentPrice?: number
    productNames?: string
    remark?: string
    matchedItemId?: number
    items?: Array<{
      id?: number
      productId?: number
      productName?: string
      count?: number
      totalPrice?: number
      remark?: string
      matched?: boolean
    }>
  } | null
  expense?: {
    id?: number
    no?: string
    status?: number
    expenseTime?: string
    expenseType?: number
    expenseTypeName?: string
    deptId?: number
    deptName?: string
    projectId?: number
    projectName?: string
    supplierId?: number
    supplierName?: string
    financeUserId?: number
    financeUserName?: string
    accountId?: number
    accountName?: string
    expensePrice?: number
    paidPrice?: number
    remainPrice?: number
    remark?: string
    matchedItemId?: number
    items?: Array<{
      id?: number
      itemName?: string
      amount?: number
      remark?: string
      assetCandidateFlag?: boolean
      matched?: boolean
    }>
  } | null
}

export const FinanceAssetApi = {
  getFinanceAssetPage: async (params: any) => {
    return await request.get({ url: '/erp/finance-asset/page', params })
  },

  getFinanceAsset: async (id: number) => {
    return await request.get({ url: `/erp/finance-asset/get?id=${id}` })
  },

  getFinanceAssetTrace: async (id: number) => {
    return await request.get<FinanceAssetTraceVO>({ url: `/erp/finance-asset/get-trace?id=${id}` })
  },

  createFinanceAsset: async (data: FinanceAssetVO) => {
    return await request.post({ url: '/erp/finance-asset/create', data })
  },

  updateFinanceAsset: async (data: FinanceAssetVO) => {
    return await request.put({ url: '/erp/finance-asset/update', data })
  },

  deleteFinanceAsset: async (ids: number[]) => {
    return await request.delete({ url: '/erp/finance-asset/delete', params: { ids: ids.join(',') } })
  },

  updateFinanceAssetStatus: async (data: { id: number; status: number }) => {
    return await request.put({ url: '/erp/finance-asset/update-status', data })
  },

  getFinanceAssetCandidatePage: async (params: any) => {
    return await request.get({ url: '/erp/finance-asset-candidate/page', params })
  },

  confirmFinanceAssetCandidate: async (data: any) => {
    return await request.post({ url: '/erp/finance-asset-candidate/confirm', data })
  },

  generateDepreciation: async (data: { period: string }) => {
    return await request.post({ url: '/erp/finance-asset-depreciation/generate', data })
  },

  getDepreciationPage: async (params: any) => {
    return await request.get({ url: '/erp/finance-asset-depreciation/page', params })
  }
}
