import {
  resolveErpAuditStatusLabel,
  resolveErpAuditStatusTagType
} from '@/utils/erpAuditStatus'

export type StockAuditStatusInput = {
  status?: number | null
  processInstanceId?: string | number | null
}

export const getStockAuditStatusLabel = (input: StockAuditStatusInput) =>
  resolveErpAuditStatusLabel(input.status ?? undefined, input.processInstanceId)

export const getStockAuditStatusTagType = (input: StockAuditStatusInput) =>
  resolveErpAuditStatusTagType(input.status ?? undefined, input.processInstanceId)
