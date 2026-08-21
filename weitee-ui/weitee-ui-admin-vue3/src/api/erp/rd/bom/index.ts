import request from '@/config/axios'

export interface RdBomItemVO {
  id?: number
  itemNo?: number
  materialId: number
  materialName?: string
  referenceDesignator?: string
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

  getRdBomTree: async (params: { bomId?: number; productId?: number }) => {
    return await request.get<RdBomTreeRespVO>({ url: '/erp/rd-bom/tree', params })
  },

  getWhereUsed: async (materialId: number) => {
    return await request.get<RdBomWhereUsedVO[]>({ url: '/erp/rd-bom/where-used', params: { materialId } })
  },

  getChangeLog: async (bomId: number) => {
    return await request.get<RdBomChangeLogVO[]>({ url: '/erp/rd-bom/change-log', params: { bomId } })
  },

  submitRdBom: async (id: number) => {
    return await request.post<string>({ url: `/erp/rd-bom/submit?id=${id}` })
  },

  cancelRdBom: async (id: number, reason?: string) => {
    const params: any = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/rd-bom/cancel', params })
  }
}
