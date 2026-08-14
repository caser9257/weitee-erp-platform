import type { ArStatementSummaryVO } from '@/api/erp/finance/ar-statement'

export const normalizeArStatementSummary = (
  payload: ArStatementSummaryVO[] | ArStatementSummaryVO | null | undefined
): ArStatementSummaryVO[] => {
  if (payload == null) return []
  return Array.isArray(payload) ? payload : [payload]
}
