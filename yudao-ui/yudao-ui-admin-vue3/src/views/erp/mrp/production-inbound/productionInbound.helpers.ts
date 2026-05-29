export const PRODUCTION_INBOUND_STATUS = {
  PENDING: 10,
  EXECUTED: 20,
  CANCELED: 30
} as const

export type ProductionInboundActionRow = {
  status?: number
  unitCost?: number
  totalCost?: number
}

const formatAmount = (value?: number) =>
  Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })

export const resolveProductionInboundStatusLabel = (status?: number) => {
  if (status === PRODUCTION_INBOUND_STATUS.PENDING) return '待入库'
  if (status === PRODUCTION_INBOUND_STATUS.EXECUTED) return '已入库'
  if (status === PRODUCTION_INBOUND_STATUS.CANCELED) return '已作废'
  return '-'
}

export const resolveProductionInboundStatusTagType = (status?: number) => {
  if (status === PRODUCTION_INBOUND_STATUS.PENDING) return 'warning'
  if (status === PRODUCTION_INBOUND_STATUS.EXECUTED) return 'success'
  if (status === PRODUCTION_INBOUND_STATUS.CANCELED) return 'info'
  return 'info'
}

export const canExecuteProductionInbound = (row: ProductionInboundActionRow) =>
  row.status === PRODUCTION_INBOUND_STATUS.PENDING

export const canCancelProductionInbound = (row: ProductionInboundActionRow) =>
  row.status === PRODUCTION_INBOUND_STATUS.PENDING

export const canRevertProductionInbound = (row: ProductionInboundActionRow) =>
  row.status === PRODUCTION_INBOUND_STATUS.EXECUTED

export const getProductionInboundCostHint = (row: ProductionInboundActionRow) => {
  const unitCost = Number(row.unitCost || 0)
  const totalCost = Number(row.totalCost || 0)
  if (unitCost <= 0 && totalCost <= 0) {
    return '成本快照未形成，当前按 0.00 入账'
  }
  return `单位成本 ${formatAmount(unitCost)}，入库成本 ${formatAmount(totalCost)}`
}
