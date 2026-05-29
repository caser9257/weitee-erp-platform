import request from '@/config/axios'

export interface BomItemSubstituteVO {
  id: number
  bomId?: number
  bomCode?: string
  bomItemId?: number
  productId?: number
  productName?: string
  materialId?: number
  materialName?: string
  substituteMaterialId: number
  substituteMaterialName?: string
  priority?: number
  replaceRatio?: number
  enableAutoRecommend?: boolean
  sort?: number
  remark?: string
  createTime?: string
}

export interface BomItemSubstitutePageReqVO {
  pageNo: number
  pageSize: number
  bomId?: number
  materialId?: number
  substituteMaterialId?: number
  enableAutoRecommend?: boolean
}

export const BomItemSubstituteApi = {
  getSubstitutePage: async (params: BomItemSubstitutePageReqVO) => {
    return await request.get<{ list: BomItemSubstituteVO[]; total: number }>({
      url: '/erp/mrp-substitute/page',
      params
    })
  }
}
