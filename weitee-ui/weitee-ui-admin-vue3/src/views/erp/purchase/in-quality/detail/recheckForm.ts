export interface RecheckFinalCountsInput {
  count: number
  firstRejectCount: number
  roundPassCount: number
}

export interface RecheckFinalCountsResult {
  finalPassCount: number
  finalRejectCount: number
}

export const deriveRecheckFinalCounts = ({
  count,
  firstRejectCount,
  roundPassCount
}: RecheckFinalCountsInput): RecheckFinalCountsResult => {
  const safeCount = Math.max(count, 0)
  const safeFirstRejectCount = Math.min(Math.max(firstRejectCount, 0), safeCount)
  const safeRoundPassCount = Math.min(Math.max(roundPassCount, 0), safeFirstRejectCount)
  const finalPassCount = safeCount - safeFirstRejectCount + safeRoundPassCount

  return {
    finalPassCount,
    finalRejectCount: Math.max(safeCount - finalPassCount, 0)
  }
}
