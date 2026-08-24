import request from '@/config/axios'

export interface RdBomItemVO {
  id?: number
  itemNo?: number
  materialId: number
  materialName?: string
  referenceDesignator?: string
  position?: string
  materialType?: number
  unitId?: number
  unitName?: string
  usageQty?: number
  lossRate?: number
  leadTimeDay?: number
  sort?: number
  remark?: string
  substitutes?: RdBomItemSubstituteVO[]
}

export interface RdBomItemSubstituteVO {
  id?: number
  substituteMaterialId: number
  substituteMaterialName?: string
  priority?: number
  replaceRatio?: number
  enableAutoRecommend?: boolean
  sort?: number
  remark?: string
}

export interface RdBomItemSaveReqVO {
  id?: number
  itemNo?: number
  materialId?: number
  referenceDesignator?: string
  position?: string
  materialType?: number
  unitId?: number
  usageQty?: number
  lossRate?: number
  leadTimeDay?: number
  sort?: number
  remark?: string
  substitutes?: RdBomItemSubstituteSaveReqVO[]
}

export interface RdBomItemSubstituteSaveReqVO {
  id?: number
  substituteMaterialId: number
  priority?: number
  replaceRatio?: number
  enableAutoRecommend?: boolean
  sort?: number
  remark?: string
}

export interface RdBomVO {
  id?: number
  bomCode: string
  productId: number
  productName?: string
  version?: string
  status: number
  processInstanceId?: string
  publishedBomId?: number
  lastPublishedTime?: string
  /** 源版本 BOM 编号（升版变更来源，首建为空） */
  sourceBomId?: number
  remark?: string
  createTime?: string
  items: RdBomItemVO[]
}

export interface RdBomPageReqVO {
  pageNo: number
  pageSize: number
  productId?: number
  bomCode?: string
  status?: number
}

export interface RdBomSaveReqVO {
  id?: number
  bomCode: string
  productId: number
  version?: string
  remark?: string
  items: RdBomItemSaveReqVO[]
}

export interface RdBomIntegrityIssueVO {
  issueType?: string
  severity?: string
  rowIndex?: number
  materialId?: number
  materialName?: string
  message?: string
}

export interface RdBomImportFailDetailVO {
  rowNumber?: number
  materialCode?: string
  reason?: string
  /** MISSING_MATERIAL / MATERIAL_NOT_APPROVED / MATERIAL_DISABLED / FORMAT_ERROR */
  issueType?: string
}

export interface RdBomPrecheckDetectedHeaderVO {
  bomCode?: string
  version?: string
  productName?: string
  productId?: number
  topLevelMissing?: boolean
}

export interface RdBomPrecheckMissingMaterialVO {
  materialCode?: string
  materialName?: string
  rowNumbers?: number[]
  topLevel?: boolean
}

export interface RdBomPrecheckUnapprovedMaterialVO {
  materialCode?: string
  materialId?: number
  productName?: string
  auditStatus?: number
  status?: number
  disabled?: boolean
  rowNumbers?: number[]
}

export interface RdBomPrecheckRowIssueVO {
  rowNumber?: number
  materialCode?: string
  reason?: string
}

export interface RdBomPrecheckResultVO {
  totalCount?: number
  readyCount?: number
  blockedCount?: number
  issueCount?: number
  readyToImport?: boolean
  detectedHeader?: RdBomPrecheckDetectedHeaderVO
  missingMaterials?: RdBomPrecheckMissingMaterialVO[]
  unapprovedMaterials?: RdBomPrecheckUnapprovedMaterialVO[]
  rowIssues?: RdBomPrecheckRowIssueVO[]
}

export interface RdBomImportResultVO {
  bomId?: number
  totalCount?: number
  successCount?: number
  failCount?: number
  failDetails?: RdBomImportFailDetailVO[]
  validationIssues?: RdBomIntegrityIssueVO[]
}

export interface RdBomTreeRespVO {
  id?: number
  bomCode?: string
  productId?: number
  productName?: string
  version?: string
  status?: number
  level?: number
  hasChildrenBom?: boolean
  childBomId?: number
  childBomCode?: string
  childBomVersion?: string
  items?: RdBomTreeItemVO[]
  children?: RdBomTreeItemVO[]
}

export interface RdBomTreeItemVO {
  id?: number
  materialId?: number
  materialName?: string
  materialType?: number
  unitId?: number
  unitName?: string
  usageQty?: number
  lossRate?: number
  referenceDesignator?: string
  position?: string
  leadTimeDay?: number
  remark?: string
  substitutes?: RdBomTreeSubstituteVO[]
  level?: number
  hasChildrenBom?: boolean
  childBomId?: number
  childBomCode?: string
  childBomVersion?: string
  children?: RdBomTreeItemVO[]
}

export interface RdBomTreeSubstituteVO {
  id?: number
  substituteMaterialId?: number
  substituteMaterialName?: string
  priority?: number
  replaceRatio?: number
  enableAutoRecommend?: boolean
  sort?: number
  remark?: string
}

export interface RdBomWhereUsedVO {
  bomId?: number
  bomCode?: string
  productId?: number
  productName?: string
  version?: string
  status?: number
  level?: number
  parents?: RdBomWhereUsedVO[]
}

export interface RdBomChangeLogVO {
  id?: number
  bomId?: number
  changeType?: string
  changeDetail?: string
  creator?: string
  createTime?: string
}

export interface RdBomVersionDiffFieldChangeVO {
  field?: string
  label?: string
  oldValue?: string
  newValue?: string
}

export interface RdBomVersionDiffItemSnapshotVO {
  usageQty?: number
  referenceDesignator?: string
  position?: string
  lossRate?: number
  leadTimeDay?: number
  remark?: string
}

export interface RdBomVersionDiffEntryVO {
  changeType?: 'ADDED' | 'REMOVED' | 'CHANGED' | 'UNCHANGED'
  materialId?: number
  materialName?: string
  materialType?: number
  oldItem?: RdBomVersionDiffItemSnapshotVO
  newItem?: RdBomVersionDiffItemSnapshotVO
  changes?: RdBomVersionDiffFieldChangeVO[]
}

export interface RdBomVersionDiffVO {
  sourceBomId?: number
  sourceVersion?: string
  targetBomId?: number
  targetVersion?: string
  addedCount?: number
  removedCount?: number
  changedCount?: number
  unchangedCount?: number
  entries?: RdBomVersionDiffEntryVO[]
}

export interface RdBomApprovalViewVO {
  bom?: RdBomVO
  /** 是否首次提交（无对比基准版本） */
  firstSubmit?: boolean
  baselineBomId?: number
  baselineVersion?: string
  diff?: RdBomVersionDiffVO
  changeLogs?: RdBomChangeLogVO[]
}

export const RdBomApi = {
  getRdBomPage: async (params: RdBomPageReqVO) => {
    return await request.get({ url: '/erp/rd-bom/page', params })
  },

  getRdBom: async (id: number) => {
    return await request.get<RdBomVO>({ url: `/erp/rd-bom/get?id=${id}` })
  },

  createRdBom: async (data: RdBomSaveReqVO) => {
    return await request.post<number>({ url: '/erp/rd-bom/create', data })
  },

  updateRdBom: async (data: RdBomSaveReqVO) => {
    return await request.put<boolean>({ url: '/erp/rd-bom/update', data })
  },

  deleteRdBom: async (id: number) => {
    return await request.delete<boolean>({ url: `/erp/rd-bom/delete?id=${id}` })
  },

  publishRdBom: async (id: number) => {
    return await request.put<boolean>({ url: `/erp/rd-bom/publish?id=${id}` })
  },

  validateRdBom: async (id: number) => {
    return await request.post<RdBomIntegrityIssueVO[]>({ url: `/erp/rd-bom/validate?id=${id}` })
  },

  downloadImportTemplate: async () => {
    return await request.download({ url: '/erp/rd-bom/get-import-template' })
  },

  precheckRdBomImport: async (
    params: { productId?: number; bomCode?: string; version?: string; remark?: string },
    file: File
  ) => {
    const data = new FormData()
    data.append('file', file)
    if (params.productId != null) data.append('productId', String(params.productId))
    if (params.bomCode) data.append('bomCode', params.bomCode)
    if (params.version) data.append('version', params.version)
    if (params.remark) data.append('remark', params.remark)
    return await request.upload<RdBomPrecheckResultVO>({ url: '/erp/rd-bom/import/precheck', data })
  },

  getRdBomTree: async (params: { bomId?: number; productId?: number }) => {
    return await request.get<RdBomTreeRespVO>({ url: '/erp/rd-bom/tree', params })
  },

  getWhereUsed: async (materialId: number) => {
    return await request.get<RdBomWhereUsedVO[]>({ url: '/erp/rd-bom/where-used', params: { materialId } })
  },

  getChangeLog: async (bomId: number) => {
    return await request.get<RdBomChangeLogVO[]>({ url: '/erp/rd-bom/change-log', params: { bomId } })
  },

  getVersionChain: async (id: number) => {
    return await request.get<RdBomVO[]>({ url: '/erp/rd-bom/version-chain', params: { id } })
  },

  getVersionDiff: async (sourceId: number, targetId: number) => {
    return await request.get<RdBomVersionDiffVO>({
      url: '/erp/rd-bom/version-diff',
      params: { sourceId, targetId }
    })
  },

  startChangeRdBom: async (id: number) => {
    return await request.post<number>({ url: `/erp/rd-bom/start-change?id=${id}` })
  },

  voidRdBom: async (id: number, reason?: string) => {
    const params: any = { id }
    if (reason) params.reason = reason
    return await request.put<boolean>({ url: '/erp/rd-bom/void', params })
  },

  unvoidRdBom: async (id: number) => {
    return await request.put<boolean>({ url: `/erp/rd-bom/unvoid?id=${id}` })
  },

  submitRdBom: async (id: number) => {
    return await request.post<string>({ url: `/erp/rd-bom/submit?id=${id}` })
  },

  cancelRdBom: async (id: number, reason?: string) => {
    const params: any = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/rd-bom/cancel', params })
  },

  getApprovalView: async (id: number | string) => {
    return await request.get<RdBomApprovalViewVO>({ url: '/erp/rd-bom/approval-view', params: { id } })
  }
}
