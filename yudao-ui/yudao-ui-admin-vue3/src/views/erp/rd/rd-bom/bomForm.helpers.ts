export type BomItemSubstituteFormData = {
  id?: number
  substituteMaterialId?: number
  priority?: number
  replaceRatio?: number
  enableAutoRecommend?: boolean
  sort?: number
  remark?: string
}

export type BomItemFormData = {
  id?: number
  materialId?: number
  materialType?: number
  unitId?: number
  usageQty?: number
  lossRate?: number
  leadTimeDay?: number
  sort?: number
  remark?: string
  substitutes: BomItemSubstituteFormData[]
}

export type BomFormData = {
  id?: number
  bomCode: string
  productId?: number
  version?: string
  status?: number
  remark?: string
  items: BomItemFormData[]
}

export type BomPayload = {
  id?: number
  bomCode: string
  productId: number
  version?: string
  remark?: string
  items: BomItemFormData[]
}

export function createEmptyBomItem(): BomItemFormData {
  return {
    remark: '',
    substitutes: []
  }
}

export function createEmptyBomSubstitute(): BomItemSubstituteFormData {
  return {
    priority: 1,
    replaceRatio: 1,
    enableAutoRecommend: true,
    sort: 0,
    remark: ''
  }
}

export function createDefaultBomFormData(): BomFormData {
  return {
    bomCode: '',
    productId: undefined,
    version: '',
    status: 0,
    remark: '',
    items: [createEmptyBomItem()]
  }
}

export function normalizeBomFormData(formData: BomFormData): BomFormData {
  return {
    ...formData,
    bomCode: formData.bomCode.trim(),
    version: formData.version?.trim() || undefined,
    remark: formData.remark?.trim() || undefined,
    items: formData.items.map((item) => ({
      ...item,
      remark: item.remark?.trim() || undefined,
      lossRate: item.lossRate === undefined || item.lossRate === null ? undefined : item.lossRate,
      leadTimeDay:
        item.leadTimeDay === undefined || item.leadTimeDay === null ? undefined : item.leadTimeDay,
      sort: item.sort === undefined || item.sort === null ? undefined : item.sort,
      substitutes: (item.substitutes || []).map((substitute) => ({
        ...substitute,
        remark: substitute.remark?.trim() || undefined,
        priority:
          substitute.priority === undefined || substitute.priority === null
            ? undefined
            : substitute.priority,
        replaceRatio:
          substitute.replaceRatio === undefined || substitute.replaceRatio === null
            ? undefined
            : substitute.replaceRatio,
        sort:
          substitute.sort === undefined || substitute.sort === null ? undefined : substitute.sort
      }))
    }))
  }
}

export function getCanSubmitBomForm(formData: BomFormData, saveSubmitting: boolean): boolean {
  if (saveSubmitting) {
    return false
  }
  if (!formData.bomCode.trim() || !formData.productId) {
    return false
  }
  if (!formData.items.length) {
    return false
  }
  return formData.items.every((item) => {
    if (!item.materialId || !item.usageQty || item.usageQty <= 0) {
      return false
    }
    const substitutes = item.substitutes || []
    if (!substitutes.length) {
      return true
    }
    const uniqueSubstituteIds = new Set<number>()
    for (const substitute of substitutes) {
      if (
        !substitute.substituteMaterialId ||
        substitute.replaceRatio === undefined ||
        substitute.replaceRatio === null ||
        substitute.replaceRatio <= 0
      ) {
        return false
      }
      if (uniqueSubstituteIds.has(substitute.substituteMaterialId)) {
        return false
      }
      uniqueSubstituteIds.add(substitute.substituteMaterialId)
    }
    return true
  })
}

export function buildBomPayload(formData: BomFormData): BomPayload {
  const normalizedFormData = normalizeBomFormData(formData)
  return {
    id: normalizedFormData.id,
    bomCode: normalizedFormData.bomCode,
    productId: normalizedFormData.productId!,
    version: normalizedFormData.version,
    remark: normalizedFormData.remark,
    items: normalizedFormData.items.map((item) => ({
      id: item.id,
      materialId: item.materialId,
      materialType: item.materialType,
      unitId: item.unitId,
      usageQty: item.usageQty,
      lossRate: item.lossRate,
      leadTimeDay: item.leadTimeDay,
      sort: item.sort,
      remark: item.remark,
      substitutes: (item.substitutes || []).map((substitute) => ({
        id: substitute.id,
        substituteMaterialId: substitute.substituteMaterialId,
        priority: substitute.priority,
        replaceRatio: substitute.replaceRatio,
        enableAutoRecommend: substitute.enableAutoRecommend,
        sort: substitute.sort,
        remark: substitute.remark
      }))
    }))
  }
}
