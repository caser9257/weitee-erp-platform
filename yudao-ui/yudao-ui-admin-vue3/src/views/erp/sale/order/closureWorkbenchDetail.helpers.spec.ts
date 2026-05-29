import assert from 'node:assert/strict'

const {
  buildSaleOrderDetailRoute,
  buildClosureTraceQuery
} = await import(new URL('./closureWorkbenchDetail.helpers.ts', import.meta.url).href)

const detailRoute = buildSaleOrderDetailRoute(123)

assert.equal(detailRoute.path, '/sales/order')
assert.deepEqual(detailRoute.query, {
  openId: '123',
  openType: 'detail',
  from: 'closure-workbench'
})

const traceQuery = buildClosureTraceQuery({
  id: 456,
  no: 'SO20260518001'
})

assert.deepEqual(traceQuery, {
  sourceOrderId: '456',
  sourceOrderNo: 'SO20260518001',
  traceFrom: 'closure-workbench'
})

const purchaseTraceQuery = buildClosureTraceQuery(
  {
    id: 789,
    no: 'SO20260518002'
  },
  { tab: 'purchase' }
)

assert.deepEqual(purchaseTraceQuery, {
  sourceOrderId: '789',
  sourceOrderNo: 'SO20260518002',
  traceFrom: 'closure-workbench',
  tab: 'purchase'
})
