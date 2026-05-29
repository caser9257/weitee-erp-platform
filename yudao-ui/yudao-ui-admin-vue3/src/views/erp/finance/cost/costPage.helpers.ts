import type { FinanceCostProjectSummaryRespVO } from '@/api/erp/finance/cost'

export type CostMetricCard = {
  label: string
  value: number
  hint: string
  emphasis?: boolean
}

const COST_TYPE_OPTIONS = [
  { label: '直接材料', value: 10 },
  { label: '直接人工', value: 20 },
  { label: '折旧', value: 30 },
  { label: '电费', value: 40 },
  { label: '其他制造费用', value: 50 }
]

export const getCostTypeOptions = () => COST_TYPE_OPTIONS

export const getCostTypeLabel = (value?: number) =>
  COST_TYPE_OPTIONS.find((item) => item.value === value)?.label || '-'

export const getDefaultCostDetailHeader = (productionOrderNo?: string) =>
  productionOrderNo ? `工单 ${productionOrderNo}` : '生产成本明细'

export const buildCostSummaryMetricCards = (
  projectSummaryList: FinanceCostProjectSummaryRespVO[]
): CostMetricCard[] => {
  const projectCount = projectSummaryList.length
  const orderCount = projectSummaryList.reduce(
    (sum, item) => sum + Number(item.productionOrderCount || 0),
    0
  )
  const totalManHour = projectSummaryList.reduce(
    (sum, item) => sum + Number(item.totalManHour || 0),
    0
  )
  const laborCost = projectSummaryList.reduce((sum, item) => sum + Number(item.laborCost || 0), 0)
  const totalCost = projectSummaryList.reduce((sum, item) => sum + Number(item.totalCost || 0), 0)

  return [
    { label: '项目数', value: projectCount, hint: '当前月份参与归集的项目数量' },
    { label: '工单数', value: orderCount, hint: '项目下已归集的生产工单数量' },
    { label: '总工时', value: totalManHour, hint: '项目工时累计' },
    { label: '人工成本', value: laborCost, hint: '人工归集金额' },
    { label: '总成本', value: totalCost, hint: '人工与制造费用汇总', emphasis: true }
  ]
}
