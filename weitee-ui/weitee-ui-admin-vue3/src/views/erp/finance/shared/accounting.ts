import { formatDate } from '@/utils/formatTime'

export const COMMON_STATUS_OPTIONS = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

export const PERIOD_STATUS_OPTIONS = [
  { label: '打开', value: 10 },
  { label: '已关账', value: 20 }
]

export const SUBJECT_TYPE_OPTIONS = [
  { label: '资产', value: 10 },
  { label: '负债', value: 20 },
  { label: '权益', value: 30 },
  { label: '收入', value: 40 },
  { label: '成本', value: 50 },
  { label: '费用', value: 60 },
  { label: '现金流量', value: 70 }
]

export const REPORT_TYPE_OPTIONS = [
  { label: '资产负债表', value: 10 },
  { label: '利润表', value: 20 },
  { label: '现金流量表', value: 30 }
]

export const REPORT_ITEM_CATEGORY_OPTIONS = [
  { label: '资产', value: 10 },
  { label: '负债', value: 20 },
  { label: '权益', value: 30 },
  { label: '收入', value: 40 },
  { label: '成本费用', value: 50 },
  { label: '现金流入', value: 60 },
  { label: '现金流出', value: 70 }
]

export const VOUCHER_STATUS_OPTIONS = [
  { label: '已生成', value: 10 },
  { label: '已审核', value: 20 },
  { label: '已过账', value: 30 },
  { label: '已冲销', value: 40 }
]

const findLabel = (options: Array<{ label: string; value: number }>, value?: number) =>
  options.find((item) => item.value === value)?.label || '-'

export const getCommonStatusLabel = (value?: number) => findLabel(COMMON_STATUS_OPTIONS, value)
export const getPeriodStatusLabel = (value?: number) => findLabel(PERIOD_STATUS_OPTIONS, value)
export const getSubjectTypeLabel = (value?: number) => findLabel(SUBJECT_TYPE_OPTIONS, value)
export const getReportTypeLabel = (value?: number) => findLabel(REPORT_TYPE_OPTIONS, value)
export const getReportItemCategoryLabel = (value?: number) =>
  findLabel(REPORT_ITEM_CATEGORY_OPTIONS, value)
export const getVoucherStatusLabel = (value?: number) => findLabel(VOUCHER_STATUS_OPTIONS, value)

export const formatAmount = (value?: number | string) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return String(value)
  }
  return numberValue.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

export const formatDateTimeValue = (value?: string) =>
  value ? formatDate(new Date(value), 'YYYY-MM-DD HH:mm:ss') : '-'

export const formatDateValue = (value?: string) =>
  value ? formatDate(new Date(value), 'YYYY-MM-DD') : '-'
