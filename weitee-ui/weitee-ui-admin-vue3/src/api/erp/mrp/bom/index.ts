import request from '@/config/axios'
import { config } from '@/config/axios/config'
import { getAccessToken, getTenantId, getVisitTenantId } from '@/utils/auth'

export interface BomItemVO {
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
  issueMode?: number
  backflushFlag?: boolean
  leadTimeDay?: number
  supplyWarehouseId?: number
  requiredStepId?: number
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
  referenceDesignator?: string
  materialType?: number
  unitId?: number
  usageQty?: number
  lossRate?: number
  issueMode?: number
  backflushFlag?: boolean
  leadTimeDay?: number
  supplyWarehouseId?: number
  requiredStepId?: number
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
  routeId?: number
  version?: string
  yieldRate?: number
  status: number
  effectiveDate?: string | Date
  expireDate?: string | Date
  sourceRdBomId?: number
  remark?: string
  /** 停用审批在途流程实例 ID；非空表示停用审批中 */
  processInstanceId?: string
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
  routeId?: number
  version?: string
  yieldRate?: number
  status: number
  effectiveDate?: string | Date
  expireDate?: string | Date
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

  // 发起制造 BOM 停用（废止）申请（BPM 审批，通过后由系统落 DISABLE）
  submitDisableApproval: async (id: number, reason?: string) => {
    const params: Record<string, any> = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/bom/lifecycle/submit', params })
  },

  // 撤回制造 BOM 停用申请
  cancelDisableApproval: async (id: number, reason?: string) => {
    const params: Record<string, any> = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/bom/lifecycle/cancel', params })
  },

  // 从研发 BOM 重新发布（管理动作，刷新 MBOM 快照）
  rePublishFromRdBom: async (rdBomId: number) => {
    return await request.post<boolean>({ url: `/erp/bom/re-publish?rdBomId=${rdBomId}` })
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

