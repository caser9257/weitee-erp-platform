export type FinishQualitySubmitCountsInput = {
  reportQty?: number
  qualifiedQty?: number
  unqualifiedQty?: number
}

export type FinishQualityInboundLinkInput = {
  id?: number
  status?: number
}

const PRODUCTION_FINISH_QUALITY_STATUS = {
  TO_INSPECT: 10,
  PARTIAL: 20,
  PASSED: 30,
  REJECTED: 40
} as const

export const roundCount = (value: number) =>
  Math.round((Number(value || 0) + Number.EPSILON) * 1000) / 1000

export const syncSubmitCounts = (
  input: FinishQualitySubmitCountsInput,
  changedField: 'qualified' | 'unqualified'
) => {
  const reportQty = Number(input.reportQty || 0)
  const qualifiedQty = Math.min(Math.max(Number(input.qualifiedQty || 0), 0), reportQty)
  const unqualifiedQty = Math.min(Math.max(Number(input.unqualifiedQty || 0), 0), reportQty)

  if (changedField === 'qualified') {
    return {
      qualifiedQty: roundCount(qualifiedQty),
      unqualifiedQty: roundCount(Math.max(reportQty - qualifiedQty, 0))
    }
  }

  return {
    qualifiedQty: roundCount(Math.max(reportQty - unqualifiedQty, 0)),
    unqualifiedQty: roundCount(unqualifiedQty)
  }
}

export const resolveFinishQualityStatusLabel = (status?: number) => {
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT) return '待质检'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PARTIAL) return '部分合格'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PASSED) return '全部合格'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.REJECTED) return '全部不合格'
  return '-'
}

export const resolveFinishQualityStatusTagType = (status?: number) => {
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT) return 'warning'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PARTIAL) return 'primary'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PASSED) return 'success'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.REJECTED) return 'danger'
  return 'info'
}

export const getFinishQualityCostDetailHeader = (productionOrderNo?: string) =>
  productionOrderNo ? `完工工单 ${productionOrderNo}` : '完工工单成本明细'

export const canViewProductionInbound = (row: FinishQualityInboundLinkInput) => {
  return (
    Number(row.id || 0) > 0 &&
    (row.status === PRODUCTION_FINISH_QUALITY_STATUS.PARTIAL ||
      row.status === PRODUCTION_FINISH_QUALITY_STATUS.PASSED)
  )
}
