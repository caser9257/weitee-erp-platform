import assert from 'node:assert/strict'

const { resolvePurchaseOrderRouteContext } = await import(
  new URL('./purchaseOrderRouteContext.helpers.ts', import.meta.url).href
)

assert.deepEqual(
  resolvePurchaseOrderRouteContext({
    from: 'purchase-in',
    purchaseOrderNo: 'CGMINI-0001',
    sourceOrderNo: 'WRONG-SOURCE-NO'
  }),
  {
    mode: 'purchase-in-focus',
    traceOrderId: undefined,
    traceOrderNo: '',
    purchaseOrderNo: 'CGMINI-0001'
  }
)

assert.deepEqual(
  resolvePurchaseOrderRouteContext({
    from: 'purchase-in',
    sourceOrderNo: 'CGMINI-LEGACY-0001'
  }),
  {
    mode: 'purchase-in-focus',
    traceOrderId: undefined,
    traceOrderNo: '',
    purchaseOrderNo: 'CGMINI-LEGACY-0001'
  }
)

assert.deepEqual(
  resolvePurchaseOrderRouteContext({
    sourceOrderId: '99',
    sourceOrderNo: 'SO-00099'
  }),
  {
    mode: 'sale-trace',
    traceOrderId: 99,
    traceOrderNo: 'SO-00099',
    purchaseOrderNo: ''
  }
)

assert.deepEqual(resolvePurchaseOrderRouteContext({}), {
  mode: 'none',
  traceOrderId: undefined,
  traceOrderNo: '',
  purchaseOrderNo: ''
})
