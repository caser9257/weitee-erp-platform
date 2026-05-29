import assert from 'node:assert/strict'

const {
  roundCount,
  syncSubmitCounts,
  resolveFinishQualityStatusLabel,
  resolveFinishQualityStatusTagType,
  getFinishQualityCostDetailHeader,
  canViewProductionInbound
} = await import(new URL('./finishQuality.helpers.ts', import.meta.url).href)

assert.equal(roundCount(1.23456), 1.235)
assert.equal(roundCount(8), 8)

assert.deepEqual(
  syncSubmitCounts(
    {
      reportQty: 10,
      qualifiedQty: 4,
      unqualifiedQty: 6
    },
    'qualified'
  ),
  {
    qualifiedQty: 4,
    unqualifiedQty: 6
  }
)

assert.deepEqual(
  syncSubmitCounts(
    {
      reportQty: 10,
      qualifiedQty: 12,
      unqualifiedQty: 0
    },
    'qualified'
  ),
  {
    qualifiedQty: 10,
    unqualifiedQty: 0
  }
)

assert.deepEqual(
  syncSubmitCounts(
    {
      reportQty: 8,
      qualifiedQty: 3,
      unqualifiedQty: 9
    },
    'unqualified'
  ),
  {
    qualifiedQty: 0,
    unqualifiedQty: 8
  }
)

assert.equal(resolveFinishQualityStatusLabel(10), '待质检')
assert.equal(resolveFinishQualityStatusLabel(20), '部分合格')
assert.equal(resolveFinishQualityStatusLabel(30), '全部合格')
assert.equal(resolveFinishQualityStatusLabel(40), '全部不合格')
assert.equal(resolveFinishQualityStatusLabel(0), '-')

assert.equal(resolveFinishQualityStatusTagType(10), 'warning')
assert.equal(resolveFinishQualityStatusTagType(20), 'primary')
assert.equal(resolveFinishQualityStatusTagType(30), 'success')
assert.equal(resolveFinishQualityStatusTagType(40), 'danger')
assert.equal(resolveFinishQualityStatusTagType(0), 'info')

assert.equal(getFinishQualityCostDetailHeader('FG-202605-01'), '完工工单 FG-202605-01')
assert.equal(getFinishQualityCostDetailHeader(''), '完工工单成本明细')

assert.equal(canViewProductionInbound({ id: 1, status: 30 }), true)
assert.equal(canViewProductionInbound({ id: 2, status: 20 }), true)
assert.equal(canViewProductionInbound({ id: 3, status: 10 }), false)
assert.equal(canViewProductionInbound({ id: undefined, status: 30 }), false)
