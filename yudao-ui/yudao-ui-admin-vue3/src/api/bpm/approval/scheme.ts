import request from '@/config/axios'

export type ApprovalSchemeVO = {
  id: number
  code: string
  name: string
  moduleCode: string
  bizType: string
  remark: string
  activeVersionId: number | null
  latestVersionId: number | null
  latestVersionNo: number | null
  latestVersionStatus: number | null
  designJson: string
  rules: ApprovalRuleVO[]
  createTime: string
  updateTime: string
}

export type ApprovalRuleVO = {
  id: number
  ruleName: string
  ruleType: string
  priority: number
  defaultRule: boolean
  conditionJson: string
  processJson: string
  enabled: boolean
}

export type ApprovalSchemePageReqVO = {
  name?: string
  moduleCode?: string
  bizType?: string
  latestVersionStatus?: number
  pageNo?: number
  pageSize?: number
}

export type ApprovalSchemeSaveReqVO = {
  id?: number
  versionId?: number
  name: string
  code: string
  moduleCode: string
  bizType: string
  remark?: string
  designJson: string
  rules?: ApprovalRuleSaveReqVO[]
}

export type ApprovalRuleSaveReqVO = {
  ruleName: string
  ruleType: string
  priority: number
  defaultRule?: boolean
  conditionJson?: string
  processJson?: string
  enabled?: boolean
}

// 查询审批方案分页
export const getApprovalSchemePage = async (params: ApprovalSchemePageReqVO) => {
  return await request.get({ url: '/bpm/approval-scheme/page', params })
}

// 查询审批方案详情
export const getApprovalScheme = async (id: number) => {
  return await request.get({ url: '/bpm/approval-scheme/get', params: { id } })
}

// 创建审批方案草稿
export const createApprovalSchemeDraft = async (data: ApprovalSchemeSaveReqVO) => {
  return await request.post({ url: '/bpm/approval-scheme/create-draft', data })
}

// 更新审批方案草稿
export const updateApprovalSchemeDraft = async (data: ApprovalSchemeSaveReqVO) => {
  return await request.put({ url: '/bpm/approval-scheme/update-draft', data })
}

// 提交审批方案待发布
export const submitApprovalScheme = async (data: { versionId: number }) => {
  return await request.put({ url: '/bpm/approval-scheme/submit', data })
}

// 发布审批方案
export const publishApprovalScheme = async (data: { versionId: number }) => {
  return await request.put({ url: '/bpm/approval-scheme/publish', data })
}

// 停用审批方案
export const disableApprovalScheme = async (versionId: number) => {
  return await request.put({ url: '/bpm/approval-scheme/disable', params: { versionId } })
}
