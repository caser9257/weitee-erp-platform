import assert from 'node:assert/strict'

import {
  buildPlanRulePayload,
  createDefaultPlanRuleFormData,
  getPlanRuleFieldLabels,
  MAKE_SUPPLY_TYPE,
  PURCHASE_SUPPLY_TYPE
} from './planRuleForm.helpers.ts'

const purchaseFormData = {
  ...createDefaultPlanRuleFormData(),
  productId: 1,
  supplyType: PURCHASE_SUPPLY_TYPE,
  safetyStock: 20,
  minOrderQty: 50,
  orderMultiple: 10,
  purchaseLeadDay: 7,
  makeLeadDay: 3,
  defaultSupplierId: 99
}

const purchasePayload = buildPlanRulePayload(purchaseFormData)

assert.equal(purchasePayload.purchaseLeadDay, 7)
assert.equal(purchasePayload.defaultSupplierId, 99)
assert.equal(purchasePayload.makeLeadDay, undefined)

const makeFormData = {
  ...createDefaultPlanRuleFormData(),
  productId: 1,
  supplyType: MAKE_SUPPLY_TYPE,
  safetyStock: 20,
  minOrderQty: 50,
  orderMultiple: 10,
  purchaseLeadDay: 7,
  makeLeadDay: 3,
  defaultSupplierId: 99
}

const makePayload = buildPlanRulePayload(makeFormData)

assert.equal(makePayload.purchaseLeadDay, undefined)
assert.equal(makePayload.defaultSupplierId, undefined)
assert.equal(makePayload.makeLeadDay, 3)

assert.deepEqual(getPlanRuleFieldLabels(PURCHASE_SUPPLY_TYPE), {
  minOrderQty: '最小采购量',
  orderMultiple: '采购包装基数'
})

assert.deepEqual(getPlanRuleFieldLabels(MAKE_SUPPLY_TYPE), {
  minOrderQty: '最小生产批量',
  orderMultiple: '生产流转批量'
})
