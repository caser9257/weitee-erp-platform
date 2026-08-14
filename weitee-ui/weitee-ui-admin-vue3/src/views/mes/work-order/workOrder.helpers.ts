export const PRODUCTION_ORDER_STATUS = {
  CREATED: 0,
  RELEASED: 10,
  FINISHED: 20,
  CLOSED: 30
} as const

export const getProductionOrderStatusLabel = (status?: number) => {
  if (status === PRODUCTION_ORDER_STATUS.CREATED) return '待下达'
  if (status === PRODUCTION_ORDER_STATUS.RELEASED) return '生产中'
  if (status === PRODUCTION_ORDER_STATUS.FINISHED) return '已完工'
  if (status === PRODUCTION_ORDER_STATUS.CLOSED) return '已关闭'
  return '-'
}

export const canReleaseProductionOrder = (status?: number) =>
  status === PRODUCTION_ORDER_STATUS.CREATED

export const canFinishProductionOrder = (status?: number) =>
  status === PRODUCTION_ORDER_STATUS.RELEASED
