import assert from 'node:assert/strict'

const { getStockAuditStatusLabel, getStockAuditStatusTagType } = await import(
  new URL('./stockAuditStatus.helpers.ts', import.meta.url).href
)

assert.equal(
  getStockAuditStatusLabel({ status: 10, processInstanceId: 'PI-STOCK-001' }),
  '审批中'
)
assert.equal(
  getStockAuditStatusTagType({ status: 10, processInstanceId: 'PI-STOCK-001' }),
  'warning'
)
assert.equal(getStockAuditStatusLabel({ status: 10, processInstanceId: '' }), '未审核')
assert.equal(getStockAuditStatusLabel({ status: 60 }), '处理失败')
