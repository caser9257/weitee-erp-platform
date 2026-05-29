import assert from 'node:assert/strict'

const { resolvePurchaseInRouteOpen } = await import(
  new URL('./purchaseInRouteOpen.helpers.ts', import.meta.url).href
)

assert.deepEqual(
  resolvePurchaseInRouteOpen({
    openAction: 'stock-execute',
    openId: 25,
    openType: 'detail',
    purchaseOrderId: 88
  }),
  {
    action: 'stock-execute',
    id: 25,
    cleanupKeys: ['openId', 'openType', 'openAction', 'purchaseOrderId', 'purchaseOrderNo', 'from']
  }
)

assert.deepEqual(
  resolvePurchaseInRouteOpen({
    openType: 'create',
    purchaseOrderId: 88
  }),
  {
    action: 'form',
    formType: 'create',
    purchaseOrderId: 88,
    cleanupKeys: ['openId', 'openType', 'openAction', 'purchaseOrderId', 'purchaseOrderNo', 'from']
  }
)

assert.deepEqual(
  resolvePurchaseInRouteOpen({
    openType: 'detail',
    openId: 18
  }),
  {
    action: 'form',
    formType: 'detail',
    id: 18,
    cleanupKeys: ['openId', 'openType', 'openAction', 'purchaseOrderId', 'purchaseOrderNo', 'from']
  }
)

assert.deepEqual(
  resolvePurchaseInRouteOpen({
    openAction: 'stock-execute'
  }),
  {
    action: 'none',
    cleanupKeys: ['openId', 'openType', 'openAction', 'purchaseOrderId', 'purchaseOrderNo', 'from']
  }
)
