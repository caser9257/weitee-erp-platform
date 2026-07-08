import assert from 'node:assert/strict'
import { deriveRecheckFinalCounts } from './recheckForm'

const shouldSetAllCountsToPassAfterFullRecheck = () => {
  const result = deriveRecheckFinalCounts({
    count: 9992,
    firstRejectCount: 24,
    roundPassCount: 24
  })

  assert.deepEqual(result, {
    finalPassCount: 9992,
    finalRejectCount: 0
  })
}

const shouldKeepRemainingRejectCountsAfterPartialRecheck = () => {
  const result = deriveRecheckFinalCounts({
    count: 9992,
    firstRejectCount: 24,
    roundPassCount: 10
  })

  assert.deepEqual(result, {
    finalPassCount: 9978,
    finalRejectCount: 14
  })
}

shouldSetAllCountsToPassAfterFullRecheck()
shouldKeepRemainingRejectCountsAfterPartialRecheck()
