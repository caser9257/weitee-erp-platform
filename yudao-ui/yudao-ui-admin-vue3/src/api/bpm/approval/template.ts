import request from '@/config/axios'

export type ApprovalTemplateVO = {
  id: number
  code: string
  name: string
  category: string
  icon: string
  description: string
  formConfig: Record<string, any> | null
  flowConfig: Record<string, any> | null
  notifyConfig: Record<string, any> | null
  useCount: number
  status: number
  sort: number
  createTime: string
}

export type ApprovalTemplatePageReqVO = {
  name?: string
  category?: string
  status?: number
  pageNo?: number
  pageSize?: number
}

// 查询审批模板分页
export const getApprovalTemplatePage = async (params: ApprovalTemplatePageReqVO) => {
  return await request.get({ url: '/bpm/approval-template/page', params })
}

// 查询审批模板详情
export const getApprovalTemplate = async (id: number) => {
  return await request.get({ url: '/bpm/approval-template/get', params: { id } })
}

// 根据模板编码查询审批模板
export const getApprovalTemplateByCode = async (code: string) => {
  return await request.get({ url: '/bpm/approval-template/get-by-code', params: { code } })
}

// 使用模板创建审批场景
export const useApprovalTemplate = async (templateId: number) => {
  return await request.post({ url: '/bpm/approval-template/use', params: { templateId } })
}
