import request from '@/config/axios'

export interface OutsourceInboundVO {
  id?: number
  inboundNo?: string
  orderId?: number
  orderNo?: string
  warehouseId?: number
  warehouseName?: string
  batchNo?: string
  inboundTime?: Date | string | number
  produceDate?: string
  expireDate?: string
  status?: number
  statusName?: string
  inboundQty?: number
  materialCost?: number
  processFee?: number
  totalCost?: number
  unitCost?: number
  remark?: string
  creatorName?: string
  createTime?: Date | string | number
  // 关联台账信息
  statementId?: number
  statementNo?: string
  statementAmount?: number
  statementPaidAmount?: number
  statementRemainAmount?: number
  statementStatus?: number
  statementStatusName?: string
}

export interface OutsourceInboundPageReqVO {
  pageNo: number
  pageSize: number
  inboundNo?: string
  orderId?: number
  status?: number
}

export interface OutsourceInboundPrintDataVO {
  outsourceInbound?: OutsourceInboundVO
  financialFacts?: {
    plannedQty?: number
    finishedQty?: number
    lossQty?: number
    pendingInboundQty?: number
    unresolvedQty?: number
    overInboundQty?: number
    materialCost?: number
    returnMaterialCost?: number
    netMaterialCost?: number
    processFee?: number
    totalCost?: number
    unitCost?: number
    issueCount?: number
    returnCount?: number
    feeCount?: number
    inboundCount?: number
    hasSupplementIssue?: boolean
  }
  sourceAttachments?: Array<{
    name?: string
    url?: string
  }>
  reconciliationRecords?: Array<{
    paymentId?: number
    paymentNo?: string
    allocateAmount?: number
    paymentTime?: Date | string | number
    operatorName?: string
    status?: number
    statusName?: string
    remark?: string
  }>
}

export const OutsourceInboundApi = {
  getOutsourceInboundPage: async (params: OutsourceInboundPageReqVO) => {
    return await request.get({ url: '/erp/outsource-order/inbound/page', params })
  },

  getOutsourceInbound: async (id: number) => {
    return await request.get({ url: `/erp/outsource-order/inbound/get?id=${id}` })
  },

  getOutsourceInboundPrintData: async (id: number) => {
    return await request.get<OutsourceInboundPrintDataVO>({
      url: `/erp/outsource-order/inbound/get-print-data?id=${id}`
    })
  }
}
