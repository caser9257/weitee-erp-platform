import { ProductUnitApi, ProductUnitTypeEnum, type ProductUnitVO } from '@/api/erp/product/unit'

let cachedUnits: ProductUnitVO[] | null = null
let loadingPromise: Promise<ProductUnitVO[]> | null = null

/** 加载单位精简列表（模块级缓存，全部单据页共享） */
export const loadProductUnits = async (): Promise<ProductUnitVO[]> => {
  if (cachedUnits) {
    return cachedUnits
  }
  if (!loadingPromise) {
    loadingPromise = ProductUnitApi.getProductUnitSimpleList()
      .then((units) => {
        cachedUnits = units || []
        return cachedUnits
      })
      .finally(() => {
        loadingPromise = null
      })
  }
  return loadingPromise
}

/** 单位数据变更后（如单位维护页保存）刷新缓存 */
export const refreshProductUnits = async (): Promise<ProductUnitVO[]> => {
  cachedUnits = null
  return loadProductUnits()
}

/** 按编号查找单位 */
export const getUnit = (units: ProductUnitVO[], unitId?: number): ProductUnitVO | undefined => {
  if (unitId == null) {
    return undefined
  }
  return units.find((unit) => unit.id === unitId)
}

/** 产品的可选单位族：基本单位 + 其启用辅助单位 */
export const getUnitFamily = (units: ProductUnitVO[], baseUnitId?: number): ProductUnitVO[] => {
  const baseUnit = getUnit(units, baseUnitId)
  if (!baseUnit) {
    return []
  }
  return [
    baseUnit,
    ...units.filter(
      (unit) =>
        unit.unitType === ProductUnitTypeEnum.AUXILIARY && unit.baseUnitId === baseUnit.id
    )
  ]
}

/** 录入数量换算为基本单位数量（仅用于前端展示提示，记账以后端为准） */
export const toBaseCount = (
  units: ProductUnitVO[],
  unitId: number | undefined,
  count: number | undefined
): number | undefined => {
  if (count == null) {
    return undefined
  }
  const unit = getUnit(units, unitId)
  if (!unit || unit.conversionRate == null) {
    return count
  }
  return Number(count) * Number(unit.conversionRate)
}

/** 单位数量精度；未配置时返回 undefined 由调用方兜底 */
export const getUnitQuantityPrecision = (
  units: ProductUnitVO[],
  unitId: number | undefined
): number | undefined => getUnit(units, unitId)?.quantityPrecision

/** 单位名称 */
export const getUnitName = (units: ProductUnitVO[], unitId: number | undefined): string =>
  getUnit(units, unitId)?.name ?? ''

/** 是否为辅助单位（需要展示换算提示） */
export const isAuxiliaryUnit = (units: ProductUnitVO[], unitId: number | undefined): boolean =>
  getUnit(units, unitId)?.unitType === ProductUnitTypeEnum.AUXILIARY

/** 录入单位对应的产品基本单位编号（辅助单位回指基本单位，基本单位即自身） */
export const resolveBaseUnitId = (
  units: ProductUnitVO[],
  unitId: number | undefined
): number | undefined => {
  const unit = getUnit(units, unitId)
  return unit?.baseUnitId ?? unitId
}

export interface UnitInputItem {
  productUnitId?: number
  baseUnitId?: number
  count?: number
  inputCount?: number
}

/**
 * 编辑回显归一化：数量按录入单位口径展示（inputCount 优先），并派生产品基本单位编号。
 * 历史单据 inputCount 为空时视为基本单位录入，无需处理。
 */
export const normalizeItemsForUnitInput = async (items?: UnitInputItem[]) => {
  if (!items?.length) {
    return
  }
  const units = await loadProductUnits()
  items.forEach((item) => {
    if (item.inputCount != null) {
      item.count = item.inputCount
    }
    item.baseUnitId = resolveBaseUnitId(units, item.productUnitId)
  })
}
