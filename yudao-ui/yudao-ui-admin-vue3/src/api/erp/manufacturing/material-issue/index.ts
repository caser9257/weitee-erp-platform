import request from '@/config/axios'

export interface ProductionIssueRecommendReqVO {
  productionMaterialId: number
  warehouseId: number
  issueQty: number
}

export interface ProductionIssueRecommendItemVO {
  stockBatchId: number
  batchNo: string
  inboundTime?: string | Date | number
  produceDate?: string
  expireDate?: string
  availableQty?: number
  recommendedQty?: number
}

export interface ProductionIssueRecommendRespVO {
  totalAvailableQty?: number
  gapQty?: number
  batchAllocations: ProductionIssueRecommendItemVO[]
}

export interface ProductionIssueCreateBatchVO {
  stockBatchId: number
  batchNo: string
  issueQty: number
}

export interface ProductionIssueCreateItemVO {
  productionMaterialId: number
  materialId: number
  warehouseId: number
  issueQty: number
  remark?: string
  batches: ProductionIssueCreateBatchVO[]
}

export interface ProductionIssueCreateReqVO {
  productionOrderId: number
  remark?: string
  items: ProductionIssueCreateItemVO[]
}

export interface ProductionIssuePageReqVO {
  pageNo: number
  pageSize: number
  issueNo?: string
  productionOrderId?: number
  status?: number
  issueTime?: [string, string]
}

export interface ProductionIssueVO {
  id?: number
  issueNo?: string
  productionOrderId?: number
  productionOrderNo?: string
  issueTime?: string | Date | number
  issueAmount?: number
  status?: number
  statusName?: string
  remark?: string
  creatorName?: string
  createTime?: string | Date | number
}

export interface ProductionIssuePrintSourceAttachmentVO {
  name?: string
  url?: string
}

export interface ProductionIssuePrintDataVO {
  financialFacts?: {
    voucherNo?: string
    voucherStatusName?: string
    costSnapshotTime?: string | Date | number
    issueItemCount?: number
    issueBatchCount?: number
    costIssueCount?: number
    materialCost?: number
    totalCost?: number
    unitCost?: number
  }
  productionIssue?: {
    id?: number
    issueNo?: string
    productionOrderId?: number
    productionOrderNo?: string
    issueTime?: string | Date | number
    issueAmount?: number
    status?: number
    statusName?: string
    remark?: string
    creator?: string
    creatorName?: string
    createTime?: string | Date | number
    items?: Array<{
      id?: number
      productionMaterialId?: number
      materialId?: number
      materialName?: string
      materialCode?: string
      materialBarCode?: string
      productUnitName?: string
      warehouseId?: number
      warehouseName?: string
      issueQty?: number
      issueAmount?: number
      remark?: string
      batches?: Array<{
        id?: number
        stockBatchId?: number
        batchNo?: string
        inboundTime?: string | Date | number
        produceDate?: string
        expireDate?: string
        issueQty?: number
        remark?: string
      }>
    }>
  }
  sourceAttachments?: ProductionIssuePrintSourceAttachmentVO[]
}

export const ProductionIssueApi = {
  recommend: async (data: ProductionIssueRecommendReqVO) => {
    return await request.post({ url: `/erp/production-material-issue/recommend`, data })
  },

  create: async (data: ProductionIssueCreateReqVO) => {
    return await request.post<number>({ url: `/erp/production-material-issue/create`, data })
  },

  getProductionIssuePage: async (params: ProductionIssuePageReqVO) => {
    return await request.get<PageResult<ProductionIssueVO[]>>({
      url: `/erp/production-material-issue/page`,
      params
    })
  },

  getPrintData: async (id: number) => {
    return await request.get<ProductionIssuePrintDataVO>({
      url: `/erp/production-material-issue/get-print-data?id=${id}`
    })
  }
}
