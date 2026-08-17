import request from '@/config/axios'

// 审批模板 VO
export interface BpmApprovalTemplateVO {
  id?: number
  code?: string
  name?: string
  category?: string
  icon?: string
  description?: string
  formConfig?: Record<string, any>
  flowConfig?: Record<string, any>
  notifyConfig?: Record<string, any>
  useCount?: number
  status?: number
  sort?: number
  createTime?: string
}

// 使用模板创建审批场景请求 VO
export interface BpmApprovalTemplateUseReqVO {
  templateId?: number
  sceneCode?: string
  sceneName?: string
}

// 审批模板 API
export const ApprovalTemplateApi = {
  // 查询审批模板分页
  getTemplatePage: async (params: any) => {
    return await request.get({ url: '/bpm/approval-template/page', params })
  },

  // 查询审批模板详情
  getTemplate: async (id: number) => {
    return await request.get({ url: '/bpm/approval-template/get?id=' + id })
  },

  // 根据模板编码查询审批模板
  getTemplateByCode: async (code: string) => {
    return await request.get({ url: '/bpm/approval-template/get-by-code?code=' + code })
  },

  // 使用模板创建审批场景和方案
  useTemplate: async (templateId: number, data?: BpmApprovalTemplateUseReqVO) => {
    return await request.post({
      url: '/bpm/approval-template/use?templateId=' + templateId,
      data: data ?? {}
    })
  }
}
