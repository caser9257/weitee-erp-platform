import assert from 'node:assert/strict'
import { getFinanceAssetStatusActionDescriptor } from './assetStatus.helpers'

assert.deepEqual(getFinanceAssetStatusActionDescriptor(0), {
  canToggleStatus: true,
  nextStatus: 10
})

assert.deepEqual(getFinanceAssetStatusActionDescriptor(10), {
  canToggleStatus: true,
  nextStatus: 20
})

assert.deepEqual(getFinanceAssetStatusActionDescriptor(20), {
  canToggleStatus: true,
  nextStatus: 10
})

assert.deepEqual(getFinanceAssetStatusActionDescriptor(30), {
  canToggleStatus: false,
  nextStatus: undefined
})
