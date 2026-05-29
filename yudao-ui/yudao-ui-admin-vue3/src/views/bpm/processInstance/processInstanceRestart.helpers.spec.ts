import assert from 'node:assert/strict'

const {
  canRestartProcessInstance,
  canRestartSaleOrderBusinessRecord,
  isSaleOrderRestartTarget,
  resolveProcessInstanceRestartTarget
} = await import(new URL('./processInstanceRestart.helpers.ts', import.meta.url).href)
const { BpmProcessInstanceStatus } = await import(
  new URL('../../../utils/constants.ts', import.meta.url).href
)

assert.equal(canRestartProcessInstance(BpmProcessInstanceStatus.RUNNING), false)
assert.equal(canRestartProcessInstance(BpmProcessInstanceStatus.APPROVE), false)
assert.equal(canRestartProcessInstance(BpmProcessInstanceStatus.REJECT), true)
assert.equal(canRestartProcessInstance(BpmProcessInstanceStatus.CANCEL), true)

assert.equal(
  canRestartSaleOrderBusinessRecord({
    status: 30,
    processInstanceId: ''
  }),
  true
)
assert.equal(
  canRestartSaleOrderBusinessRecord({
    status: 10,
    processInstanceId: ''
  }),
  true
)
assert.equal(
  canRestartSaleOrderBusinessRecord({
    status: 10,
    processInstanceId: 'abc'
  }),
  false
)
assert.equal(
  canRestartSaleOrderBusinessRecord({
    status: 20,
    processInstanceId: ''
  }),
  false
)

assert.equal(
  isSaleOrderRestartTarget({
    processDefinitionKey: 'erp_sale_order'
  }),
  true
)
assert.equal(
  isSaleOrderRestartTarget({
    formCustomCreatePath: '/erp/sale/order'
  }),
  true
)
assert.equal(
  isSaleOrderRestartTarget({
    processDefinitionKey: 'erp_purchase_order',
    formCustomCreatePath: '/erp/purchase/order'
  }),
  false
)

assert.deepEqual(
  resolveProcessInstanceRestartTarget({
    formType: 20,
    processDefinitionKey: 'erp_sale_order',
    businessKey: '123'
  }),
  {
    name: 'ErpSaleOrderRestartPage',
    query: {
      id: '123',
      from: 'processInstanceRestart'
    }
  }
)

assert.deepEqual(
  resolveProcessInstanceRestartTarget({
    formType: 20,
    formCustomCreatePath: '/erp/sale/order',
    businessKey: 456
  }),
  {
    name: 'ErpSaleOrderRestartPage',
    query: {
      id: '456',
      from: 'processInstanceRestart'
    }
  }
)

assert.deepEqual(
  resolveProcessInstanceRestartTarget({
    formType: 20,
    formCustomCreatePath: '/erp/purchase/order',
    businessKey: '789'
  }),
  {
    path: '/erp/purchase/order',
    query: {
      id: '789'
    }
  }
)

assert.deepEqual(
  resolveProcessInstanceRestartTarget({
    formType: 10,
    processInstanceId: 88
  }),
  {
    name: 'BpmProcessInstanceCreate',
    query: {
      processInstanceId: '88'
    }
  }
)
