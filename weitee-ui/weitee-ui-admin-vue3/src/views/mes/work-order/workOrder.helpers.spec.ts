import assert from 'node:assert/strict'
import {
  PRODUCTION_ORDER_STATUS,
  canFinishProductionOrder,
  canReleaseProductionOrder,
  getProductionOrderStatusLabel
} from './workOrder.helpers'

assert.equal(getProductionOrderStatusLabel(PRODUCTION_ORDER_STATUS.CREATED), '待下达')
assert.equal(getProductionOrderStatusLabel(PRODUCTION_ORDER_STATUS.RELEASED), '生产中')
assert.equal(getProductionOrderStatusLabel(PRODUCTION_ORDER_STATUS.FINISHED), '已完工')
assert.equal(getProductionOrderStatusLabel(PRODUCTION_ORDER_STATUS.CLOSED), '已关闭')

assert.equal(canReleaseProductionOrder(PRODUCTION_ORDER_STATUS.CREATED), true)
assert.equal(canReleaseProductionOrder(PRODUCTION_ORDER_STATUS.RELEASED), false)
assert.equal(canFinishProductionOrder(PRODUCTION_ORDER_STATUS.RELEASED), true)
assert.equal(canFinishProductionOrder(PRODUCTION_ORDER_STATUS.CREATED), false)
assert.equal(canFinishProductionOrder(PRODUCTION_ORDER_STATUS.FINISHED), false)
