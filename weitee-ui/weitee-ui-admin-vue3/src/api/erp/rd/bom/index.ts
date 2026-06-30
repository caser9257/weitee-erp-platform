import request from '@/config/axios'

export interface RdBomItemVO {
  id?: number
  itemNo?: number
  materialId: number
  materialName?: string
  materialNameSnapshot?: string
  specModelSnapshot?: string
  materialDescSnapshot?: string
  referenceDesignator?: string
  packageTypeSnapshot?: string
  qualityGradeSnapshot?: string
  keyPart?: boolean
  supplierId?: number
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
  materialNameSnapshot?: string
  specModelSnapshot?: string
  materialDescSnapshot?: string
  referenceDesignator?: string
  packageTypeSnapshot?: string
  qualityGradeSnapshot?: string
  keyPart?: boolean
  supplierId?: number
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
  }
}
