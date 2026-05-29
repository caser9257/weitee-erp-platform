import request from '@/config/axios'

export interface OutsourceFeeVO {
  id?: number
  feeNo?: string
  orderId?: number
  orderNo?: string
  statementId?: number
  statementNo?: string
  feeTime?: Date | string | number
  status?: number
  statusName?: string
  feeAmount?: number
  paidAmount?: number
  remainAmount?: number
  reconciliationStatus?: number
  reconciliationStatusName?: string
  remark?: string
  creatorName?: string
  createTime?: Date | string | number
}

export const OutsourceFeeApi = {
  getOutsourceFee: async (id: number) => {
    return await request.get<OutsourceFeeVO>({ url: `/erp/outsource-order/fee/get?id=${id}` })
  }
}
