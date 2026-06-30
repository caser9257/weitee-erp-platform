export type StockTone = 'blue' | 'emerald' | 'amber' | 'rose' | 'slate'

const PILL_BASE_CLASS =
  'inline-flex max-w-[180px] items-center rounded-full border px-2.5 py-0.5 text-[12px] font-semibold leading-5 whitespace-nowrap overflow-hidden text-ellipsis transition-colors duration-200'

const CARD_BASE_CLASS =
  'overflow-hidden rounded-2xl border border-slate-200 border-t-4 bg-white shadow-sm transition-all duration-200'

const PILL_TONE_CLASS_MAP: Record<StockTone, string> = {
  blue: 'border-blue-100 bg-blue-50 text-blue-600',
  emerald: 'border-emerald-100 bg-emerald-50 text-emerald-600',
  amber: 'border-amber-100 bg-amber-50 text-amber-600',
  rose: 'border-rose-100 bg-rose-50 text-rose-600',
  slate: 'border-slate-200 bg-slate-100 text-slate-600'
}

const CARD_TONE_CLASS_MAP: Record<StockTone, string> = {
  blue: 'border-blue-100 bg-blue-50 border-t-blue-500',
  emerald: 'border-emerald-100 bg-emerald-50 border-t-emerald-500',
  amber: 'border-amber-100 bg-amber-50 border-t-amber-500',
  rose: 'border-rose-100 bg-rose-50 border-t-rose-500',
  slate: 'border-slate-200 bg-slate-50 border-t-slate-400'
}

const TEXT_CLASS_MAP: Record<StockTone, string> = {
  blue: 'text-blue-600',
  emerald: 'text-emerald-600',
  amber: 'text-amber-600',
  rose: 'text-rose-600',
  slate: 'text-slate-600'
}

export const getTonePillClass = (tone: StockTone) => `${PILL_BASE_CLASS} ${PILL_TONE_CLASS_MAP[tone]}`

export const getToneCardClass = (tone: StockTone) => `${CARD_BASE_CLASS} ${CARD_TONE_CLASS_MAP[tone]}`

export const getToneTextClass = (tone: StockTone) => TEXT_CLASS_MAP[tone]

export const resolveCommonStatusTone = (value?: number | string | boolean): StockTone => {
  const normalized = Number(value)
  if (normalized === 0) {
    return 'emerald'
  }
  if (normalized === 1) {
    return 'rose'
  }
  return 'blue'
}

export const resolveStockBizTone = (value?: number | string | boolean): StockTone => {
  const normalized = Number(value)
  if ([10, 30, 40, 60, 70, 100].includes(normalized)) {
    return 'emerald'
  }
  if ([20, 32, 42, 50, 80, 101].includes(normalized)) {
    return 'rose'
  }
  if ([11, 21, 31, 33, 41, 43, 51, 61, 71, 81].includes(normalized)) {
    return 'amber'
  }
  return 'blue'
}

export const resolveSummaryTone = (index: number): StockTone => {
  const tones: StockTone[] = ['blue', 'emerald', 'amber', 'rose']
  return tones[index % tones.length]
}

export const resolveCountTone = (value?: number | string | null): StockTone => {
  const normalized = Number(value || 0)
  if (normalized < 0) {
    return 'rose'
  }
  if (normalized > 0) {
    return 'emerald'
  }
  return 'slate'
}

export const resolveBizToneClass = (value?: number | string | boolean) =>
  getTonePillClass(resolveStockBizTone(value))

export const resolveWarehouseTone = (value?: string | null): StockTone => {
  const normalized = (value || '').trim()
  if (normalized.includes('成品')) {
    return 'emerald'
  }
  if (normalized.includes('在途')) {
    return 'amber'
  }
  if (normalized.includes('在建')) {
    return 'amber'
  }
  if (normalized.includes('原料')) {
    return 'amber'
  }
  if (normalized.includes('测试')) {
    return 'blue'
  }
  return 'slate'
}

export const resolveSummaryCardClass = (index: number) => getToneCardClass(resolveSummaryTone(index))

export const resolveDictLabel = (
  options: Array<{ value: number | string | boolean; label: string }>,
  value?: number | string | boolean | null
) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  return options.find((item) => String(item.value) === String(value))?.label || '-'
}
