import assert from 'node:assert/strict'

const {
  PRODUCTION_INBOUND_STATUS,
  resolveProductionInboundStatusLabel,
  resolveProductionInboundStatusTagType,
  canExecuteProductionInbound,
  canCancelProductionInbound,
  canRevertProductionInbound,
  getProductionInboundCostHint
} = await import(new URL('./productionInbound.helpers.ts', import.meta.url).href)

assert.equal(PRODUCTION_INBOUND_STATUS.PENDING, 10)
assert.equal(PRODUCTION_INBOUND_STATUS.EXECUTED, 20)
assert.equal(PRODUCTION_INBOUND_STATUS.CANCELED, 30)

assert.equal(resolveProductionInboundStatusLabel(10), '待入库')
assert.equal(resolveProductionInboundStatusLabel(20), '已入库')
assert.equal(resolveProductionInboundStatusLabel(30), '已作废')
assert.equal(resolveProductionInboundStatusLabel(0), '-')

assert.equal(resolveProductionInboundStatusTagType(10), 'warning')
assert.equal(resolveProductionInboundStatusTagType(20), 'success')
assert.equal(resolveProductionInboundStatusTagType(30), 'info')
assert.equal(resolveProductionInboundStatusTagType(0), 'info')

assert.equal(canExecuteProductionInbound({ status: 10 }), true)
assert.equal(canExecuteProductionInbound({ status: 20 }), false)

assert.equal(canCancelProductionInbound({ status: 10 }), true)
assert.equal(canCancelProductionInbound({ status: 30 }), false)

assert.equal(canRevertProductionInbound({ status: 20 }), true)
assert.equal(canRevertProductionInbound({ status: 10 }), false)

assert.equal(
  getProductionInboundCostHint({
    unitCost: 0,
    totalCost: 0
  }),
  '成本快照未形成，当前按 0.00 入账'
)

assert.equal(
  getProductionInboundCostHint({
    unitCost: 12.3456,
    totalCost: 1234.56
  }),
  '单位成本 12.35，入库成本 1,234.56'
)
