import request from '@/config/axios'

export interface StockTaskFailureVO {
  id: number
  bizType: string
  bizId: number
  itemId?: number
  productId: number
  warehouseId: number
  qty: number
  reason?: string
  status?: number
  retryCount?: number
  createTime?: string | Date | number
}

export const STOCK_TASK_FAILURE_BIZ_TYPE = {
  PRODUCTION_DEDUCT: 'PRODUCTION_DEDUCT',
  IQC_MOVE_AVAILABLE: 'IQC_MOVE_AVAILABLE'
} as const

export const STOCK_TASK_FAILURE_STATUS = {
  PENDING_RETRY: 0,
  RESOLVED: 1
} as const

export const StockTaskFailureApi = {
  getFailureLogList: async (status?: number) => {
    return await request.get<StockTaskFailureVO[]>({
      url: '/erp/stock-task-failure/list',
      params: status === undefined || status === null ? {} : { status }
    })
  },

  retryFailure: async (id: number) => {
    return await request.post<boolean>({ url: `/erp/stock-task-failure/retry?id=${id}` })
  }
}
