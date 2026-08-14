import assert from 'node:assert/strict'
import {
  PRODUCTION_INBOUND_STATUS,
  canExecuteProductionInbound,
  canRevertProductionInbound,
  getProductionInboundStatusLabel
} from './productionInbound.helpers'

assert.equal(getProductionInboundStatusLabel(PRODUCTION_INBOUND_STATUS.PENDING), '待入库')
assert.equal(getProductionInboundStatusLabel(PRODUCTION_INBOUND_STATUS.EXECUTED), '已入库')
assert.equal(getProductionInboundStatusLabel(PRODUCTION_INBOUND_STATUS.CANCELED), '已作废')

assert.equal(canExecuteProductionInbound(PRODUCTION_INBOUND_STATUS.PENDING), true)
assert.equal(canExecuteProductionInbound(PRODUCTION_INBOUND_STATUS.EXECUTED), false)
assert.equal(canExecuteProductionInbound(PRODUCTION_INBOUND_STATUS.CANCELED), false)

assert.equal(canRevertProductionInbound(PRODUCTION_INBOUND_STATUS.PENDING), false)
assert.equal(canRevertProductionInbound(PRODUCTION_INBOUND_STATUS.EXECUTED), true)
assert.equal(canRevertProductionInbound(PRODUCTION_INBOUND_STATUS.CANCELED), false)
