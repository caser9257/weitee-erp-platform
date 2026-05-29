import assert from 'node:assert/strict'

const {
  buildCostSummaryMetricCards,
  getCostTypeOptions,
  getCostTypeLabel,
  getDefaultCostDetailHeader
} = await import(new URL('./costPage.helpers.ts', import.meta.url).href)

const summaryCards = buildCostSummaryMetricCards([
  {
    projectId: 1,
    projectNo: 'PRJ-001',
    projectName: '精密模组项目',
    productionOrderCount: 2,
    productCount: 3,
    totalManHour: 12.5,
    laborCost: 1000,
    depreciationCost: 500,
    powerCost: 300,
    otherCost: 200,
    totalCost: 2000
  },
  {
    projectId: 2,
    projectNo: 'PRJ-002',
    projectName: '自动化产线项目',
    productionOrderCount: 1,
    productCount: 1,
    totalManHour: 7.5,
    laborCost: 600,
    depreciationCost: 200,
    powerCost: 100,
    otherCost: 100,
    totalCost: 1000
  }
])

assert.equal(summaryCards[0].label, '项目数')
assert.equal(summaryCards[0].value, 2)
assert.equal(summaryCards[1].label, '工单数')
assert.equal(summaryCards[1].value, 3)
assert.equal(summaryCards[2].label, '总工时')
assert.equal(summaryCards[2].value, 20)
assert.equal(summaryCards[3].label, '人工成本')
assert.equal(summaryCards[3].value, 1600)
assert.equal(summaryCards[4].label, '总成本')
assert.equal(summaryCards[4].value, 3000)
assert.equal(summaryCards[4].emphasis, true)

const costTypeOptions = getCostTypeOptions()
assert.deepEqual(costTypeOptions, [
  { label: '直接材料', value: 10 },
  { label: '直接人工', value: 20 },
  { label: '折旧', value: 30 },
  { label: '电费', value: 40 },
  { label: '其他制造费用', value: 50 }
])

assert.equal(getCostTypeLabel(10), '直接材料')
assert.equal(getCostTypeLabel(20), '直接人工')
assert.equal(getCostTypeLabel(50), '其他制造费用')
assert.equal(getCostTypeLabel(999), '-')

assert.equal(getDefaultCostDetailHeader(undefined), '生产成本明细')
assert.equal(getDefaultCostDetailHeader('MO-202605-001'), '工单 MO-202605-001')
