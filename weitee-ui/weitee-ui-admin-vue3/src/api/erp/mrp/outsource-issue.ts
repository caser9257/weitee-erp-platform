import request from '@/config/axios'

export interface OutsourceIssueVO {
  id?: number
  issueNo?: string
  orderId?: number
  orderNo?: string
  issueType?: number
  issueTypeName?: string
  issueTime?: Date | string | number
  status?: number
  statusName?: string
  issueQty?: number
  issueAmount?: number
  remark?: string
  creatorName?: string
  createTime?: Date | string | number
  items?: Array<{
    id?: number
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
      issueQty?: number
      issueAmount?: number
      inboundTime?: Date | string | number
      produceDate?: string
      expireDate?: string
    }>
  }>
}

export interface OutsourceIssuePageReqVO {
  pageNo: number
  pageSize: number
  issueNo?: string
  orderId?: number
  issueType?: number
  status?: number
}

export interface OutsourceIssuePrintDataVO {
  outsourceIssue?: OutsourceIssueVO
  financialFacts?: {
    plannedQty?: number
    finishedQty?: number
    issueQty?: number
    issueAmount?: number
    materialCost?: number
    returnMaterialCost?: number
    netMaterialCost?: number
    processFee?: number
    totalCost?: number
    unitCost?: number
    issueItemCount?: number
    issueBatchCount?: number
    issueCount?: number
    returnCount?: number
    feeCount?: number
    inboundCount?: number
  }
  sourceAttachments?: Array<{
    name?: string
    url?: string
  }>
}

export const OutsourceIssueApi = {
  getOutsourceIssuePage: async (params: OutsourceIssuePageReqVO) => {
    return await request.get<PageResult<OutsourceIssueVO[]>>({
      url: '/erp/outsource-order/issue/page',
      params
    })
  },

  getOutsourceIssue: async (id: number) => {
    return await request.get<OutsourceIssueVO>({
      url: `/erp/outsource-order/issue/get?id=${id}`
    })
  },

  getOutsourceIssuePrintData: async (id: number) => {
    return await request.get<OutsourceIssuePrintDataVO>({
      url: `/erp/outsource-order/issue/get-print-data?id=${id}`
    })
  }
}
