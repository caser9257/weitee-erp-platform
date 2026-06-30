export const PURCHASE_SUPPLY_TYPE = 'PURCHASE'
export const MAKE_SUPPLY_TYPE = 'MAKE'
export const LOT_FOR_LOT_REPLENISH_MODE = 'LOT_FOR_LOT'
export const FIXED_LOT_REPLENISH_MODE = 'FIXED_LOT'
export const VALIDATION_STATUS_COMPLETE = 'COMPLETE'
export const VALIDATION_STATUS_INCOMPLETE = 'INCOMPLETE'

const ENABLE_STATUS = 0

export const REPLENISH_MODE_OPTIONS = [
  { label: '按需补货', value: LOT_FOR_LOT_REPLENISH_MODE },
  { label: '固定批量', value: FIXED_LOT_REPLENISH_MODE }
]

export type PlanRuleFormData = {
  id: number | undefined
  productId: number | undefined
  supplyType: string | undefined
  replenishMode: string
  safetyStock: number | undefined
  minOrderQty: number | undefined
  orderMultiple: number | undefined
  fixedOrderQty: number | undefined
  purchaseLeadDay: number | undefined
  makeLeadDay: number | undefined
  shortageWarnFlag: boolean
  defaultSupplierId: number | undefined
  remark: string
  status: number
}

export type PlanRulePayload = {
  id: number
  productId: number
  supplyType: string
  replenishMode: string
  safetyStock?: number
  minOrderQty?: number
  orderMultiple?: number
  fixedOrderQty?: number
  purchaseLeadDay?: number
  makeLeadDay?: number
  enableFlag: boolean
  shortageWarnFlag: boolean
  defaultSupplierId?: number
  remark?: string
}

export function createDefaultPlanRuleFormData(): PlanRuleFormData {
  return {
    id: undefined,
    productId: undefined,
    supplyType: undefined,
    replenishMode: LOT_FOR_LOT_REPLENISH_MODE,
    safetyStock: 0,
    minOrderQty: 0,
    orderMultiple: 1,
    fixedOrderQty: undefined,
    purchaseLeadDay: 0,
    makeLeadDay: 0,
    shortageWarnFlag: true,
    defaultSupplierId: undefined,
    remark: '',
    status: ENABLE_STATUS
  }
}

export function isPurchaseSupplyType(supplyType?: string) {
  return supplyType === PURCHASE_SUPPLY_TYPE
}

export function isMakeSupplyType(supplyType?: string) {
  return supplyType === MAKE_SUPPLY_TYPE
}

export function isFixedLotMode(replenishMode?: string) {
  return replenishMode === FIXED_LOT_REPLENISH_MODE
}

export function getPlanRuleFieldLabels(supplyType?: string) {
  if (supplyType === MAKE_SUPPLY_TYPE) {
    return {
      minOrderQty: '最小生产批量',
      orderMultiple: '生产流转批量'
    }
  }
  return {
    minOrderQty: '最小采购量',
    orderMultiple: '采购包装倍量'
  }
}

export function getReplenishModeLabel(replenishMode?: string) {
  return REPLENISH_MODE_OPTIONS.find((item) => item.value === replenishMode)?.label || '未配置'
}

export function getValidationStatusLabel(validationStatus?: string) {
  return validationStatus === VALIDATION_STATUS_COMPLETE ? '规则完整' : '待完善'
}

export function getValidationTagType(validationStatus?: string) {
  return validationStatus === VALIDATION_STATUS_COMPLETE ? 'success' : 'warning'
}

export function normalizePlanRuleFormData(formData: PlanRuleFormData): PlanRuleFormData {
  const normalized = { ...formData }
  if (isPurchaseSupplyType(normalized.supplyType)) {
    normalized.makeLeadDay = undefined
  }
  if (isMakeSupplyType(normalized.supplyType)) {
    normalized.purchaseLeadDay = undefined
    normalized.defaultSupplierId = undefined
  }
  if (!isFixedLotMode(normalized.replenishMode)) {
    normalized.fixedOrderQty = undefined
  }
  return normalized
}

export function buildPlanRulePayload(formData: PlanRuleFormData): PlanRulePayload {
  const normalizedFormData = normalizePlanRuleFormData(formData)
  return {
    id: normalizedFormData.id || 0,
    productId: normalizedFormData.productId!,
    supplyType: normalizedFormData.supplyType!,
    replenishMode: normalizedFormData.replenishMode,
    safetyStock: normalizedFormData.safetyStock,
    minOrderQty: normalizedFormData.minOrderQty,
    orderMultiple: normalizedFormData.orderMultiple,
    fixedOrderQty: normalizedFormData.fixedOrderQty,
    purchaseLeadDay: normalizedFormData.purchaseLeadDay,
    makeLeadDay: normalizedFormData.makeLeadDay,
    enableFlag: normalizedFormData.status === ENABLE_STATUS,
    shortageWarnFlag: normalizedFormData.shortageWarnFlag,
    defaultSupplierId: normalizedFormData.defaultSupplierId,
    remark: normalizedFormData.remark?.trim() || undefined
  }
}
