import request from '@/config/axios'
import { config } from '@/config/axios/config'
import { getAccessToken, getTenantId, getVisitTenantId } from '@/utils/auth'

export interface BomItemVO {
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
  mrpEnableFlag?: boolean
  supplyOwner?: string
  sort?: number
  remark?: string
  level?: number
  childBomId?: number
  childBomCode?: string
  childBomVersion?: string
  hasChildrenBom?: boolean
  children?: BomItemVO[]
  substitutes?: BomItemSubstituteVO[]
}

export interface BomItemSubstituteVO {
  id?: number
  substituteMaterialId: number
  substituteMaterialName?: string
  priority?: number
  replaceRatio?: number
  enableAutoRecommend?: boolean
  sort?: number
  remark?: string
}

export interface BomItemSaveReqVO {
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
  mrpEnableFlag?: boolean
  supplyOwner?: string
  sort?: number
  remark?: string
  substitutes?: BomItemSubstituteSaveReqVO[]
}

export interface BomItemSubstituteSaveReqVO {
  id?: number
  substituteMaterialId: number
  substituteMaterialName?: string
  priority?: number
  replaceRatio?: number
  enableAutoRecommend?: boolean
  sort?: number
  remark?: string
}

export interface BomPricingPreviewItemVO {
  materialId?: number
  materialName?: string
  bomId?: number
  bomCode?: string
  bomVersion?: string
  level?: number
  requiredQty?: number
  unitPrice?: number
  lineCost?: number
  materialType?: number
  supplyOwner?: string
  remark?: string
}

export interface BomPricingPreviewMissingMaterialVO {
  materialId?: number
  materialName?: string
  bomId?: number
  level?: number
  requiredQty?: number
  reason?: string
}

export interface BomPricingPreviewVO {
  productId?: number
  productName?: string
  bomId?: number
  bomCode?: string
  bomVersion?: string
  materialUnitPrice?: number
  calcStatus?: string
  calcMessage?: string
  items?: BomPricingPreviewItemVO[]
  missingMaterials?: BomPricingPreviewMissingMaterialVO[]
}

export interface BomVO {
  id?: number
  bomCode: string
  productId: number
  productName?: string
  version?: string
  status: number
  sourceRdBomId?: number
  remark?: string
  createTime?: string | number
  items: BomItemVO[]
}

export interface BomPageReqVO {
  pageNo: number
  pageSize: number
  productId?: number
  bomCode?: string
  status?: number
}

export interface BomTreeReqVO {
  productId?: number
  bomId?: number
}

export interface BomSaveReqVO {
  id?: number
  bomCode: string
  productId: number
  version?: string
  status: number
  remark?: string
  items: BomItemSaveReqVO[]
}

export const BomApi = {
  getBomPage: async (params: BomPageReqVO) => {
    return await request.get({ url: '/erp/bom/page', params })
  },

  getBom: async (id: number) => {
    return await request.get<BomVO>({ url: `/erp/bom/get?id=${id}` })
  },

  createBom: async (data: BomSaveReqVO) => {
    return await request.post<number>({ url: '/erp/bom/create', data })
  },

  updateBom: async (data: BomSaveReqVO) => {
    return await request.put<boolean>({ url: '/erp/bom/update', data })
  },

  updateBomStatus: async (id: number, status: number) => {
    return await request.put<boolean>({ url: `/erp/bom/update-status?id=${id}&status=${status}` })
  },

  deleteBom: async (id: number) => {
    return await request.delete<boolean>({ url: `/erp/bom/delete?id=${id}` })
  },

  getBomTree: async (params: BomTreeReqVO | number) => {
    const query = typeof params === 'number' ? { productId: params } : params
    return await request.get<BomVO>({ url: '/erp/bom/tree', params: query })
  },

  getBomPricingPreview: async (productId: number) => {
    return await request.get<BomPricingPreviewVO>({ url: `/erp/bom/pricing-preview?productId=${productId}` })
  },

  getBomPricingPreviewSilent: async (productId: number) => {
    const headers = new Headers({
      'Cache-Control': 'no-cache',
      Pragma: 'no-cache'
    })
    const accessToken = getAccessToken()
    if (accessToken) {
      headers.set('Authorization', 'Bearer ' + accessToken)
    }
    if (import.meta.env.VITE_APP_TENANT_ENABLE === 'true') {
      const tenantId = getTenantId()
      if (tenantId) {
        headers.set('tenant-id', String(tenantId))
      }
      const visitTenantId = getVisitTenantId()
      if (accessToken && visitTenantId) {
        headers.set('visit-tenant-id', String(visitTenantId))
      }
    }

    const response = await fetch(`${config.base_url}/erp/bom/pricing-preview?productId=${productId}`, {
      method: 'GET',
      headers
    })
    if (!response.ok) {
      return undefined
    }
    const payload = await response.json().catch(() => undefined)
    // Keep the same success-code behavior as the shared axios pipeline.
    const code = payload?.code || config.result_code
    if (!payload || code != config.result_code) {
      return undefined
    }
    return payload.data as BomPricingPreviewVO
  }
}

