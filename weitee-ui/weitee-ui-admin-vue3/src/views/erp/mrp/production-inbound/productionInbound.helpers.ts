export const PRODUCTION_INBOUND_STATUS = {
  PENDING: 10,
  EXECUTED: 20,
  CANCELED: 30
} as const

export const getProductionInboundStatusLabel = (status?: number) => {
  if (status === PRODUCTION_INBOUND_STATUS.PENDING) return '待入库'
  if (status === PRODUCTION_INBOUND_STATUS.EXECUTED) return '已入库'
  if (status === PRODUCTION_INBOUND_STATUS.CANCELED) return '已作废'
  return '-'
}

export const canExecuteProductionInbound = (status?: number) =>
  status === PRODUCTION_INBOUND_STATUS.PENDING

export const canRevertProductionInbound = (status?: number) =>
  status === PRODUCTION_INBOUND_STATUS.EXECUTED
