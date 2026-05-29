import assert from 'node:assert/strict'

const {
  buildPlanRulePayload,
  createDefaultPlanRuleFormData,
  FIXED_LOT_REPLENISH_MODE,
  getPlanRuleFieldLabels,
  getReplenishModeLabel,
  getValidationStatusLabel,
  isFixedLotMode,
  LOT_FOR_LOT_REPLENISH_MODE,
  MAKE_SUPPLY_TYPE,
  PURCHASE_SUPPLY_TYPE,
  VALIDATION_STATUS_COMPLETE,
  VALIDATION_STATUS_INCOMPLETE
} = await import(new URL('./planRuleForm.helpers.ts', import.meta.url).href)

const purchaseFormData = {
  ...createDefaultPlanRuleFormData(),
  productId: 1,
  supplyType: PURCHASE_SUPPLY_TYPE,
  replenishMode: LOT_FOR_LOT_REPLENISH_MODE,
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
assert.equal(purchasePayload.fixedOrderQty, undefined)

const makeFixedLotFormData = {
  ...createDefaultPlanRuleFormData(),
  productId: 1,
  supplyType: MAKE_SUPPLY_TYPE,
  replenishMode: FIXED_LOT_REPLENISH_MODE,
  safetyStock: 20,
  minOrderQty: 50,
  orderMultiple: 10,
  fixedOrderQty: 24,
  purchaseLeadDay: 7,
  makeLeadDay: 3,
  defaultSupplierId: 99
}

const makePayload = buildPlanRulePayload(makeFixedLotFormData)

assert.equal(makePayload.purchaseLeadDay, undefined)
assert.equal(makePayload.defaultSupplierId, undefined)
assert.equal(makePayload.makeLeadDay, 3)
assert.equal(makePayload.fixedOrderQty, 24)

assert.deepEqual(getPlanRuleFieldLabels(PURCHASE_SUPPLY_TYPE), {
  minOrderQty: '最小采购量',
  orderMultiple: '采购包装倍量'
})

assert.deepEqual(getPlanRuleFieldLabels(MAKE_SUPPLY_TYPE), {
  minOrderQty: '最小生产批量',
  orderMultiple: '生产流转批量'
})

assert.equal(getReplenishModeLabel(LOT_FOR_LOT_REPLENISH_MODE), '按需补货')
assert.equal(getReplenishModeLabel(FIXED_LOT_REPLENISH_MODE), '固定批量')
assert.equal(isFixedLotMode(FIXED_LOT_REPLENISH_MODE), true)
assert.equal(getValidationStatusLabel(VALIDATION_STATUS_COMPLETE), '规则完整')
assert.equal(getValidationStatusLabel(VALIDATION_STATUS_INCOMPLETE), '待完善')
