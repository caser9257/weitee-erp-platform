import request from '@/config/axios'

// 审批方案规则 VO
export interface BpmApprovalRuleVO {
  id?: number
  ruleName?: string
  ruleType?: string
  priority?: number
  defaultRule?: boolean
  conditionJson?: string
  processJson?: string
  enabled?: boolean
}

// 审批方案 VO
export interface BpmApprovalSchemeVO {
  id?: number
  code?: string
  name?: string
  moduleCode?: string
  bizType?: string
  remark?: string
  activeVersionId?: number
  latestVersionId?: number
  latestVersionNo?: number
  latestVersionStatus?: number
  designJson?: string
  rules?: BpmApprovalRuleVO[]
  createTime?: string
  updateTime?: string
}

// 审批方案保存请求 VO
export interface BpmApprovalSchemeSaveReqVO {
  id?: number
  versionId?: number
  name: string
  code: string
  moduleCode: string
  bizType: string
  remark?: string
  designJson?: string
  notifyJson?: string
  rules?: BpmApprovalRuleVO[]
}

// 审批方案 API
export const ApprovalSchemeApi = {
  // 查询审批方案分页
  getSchemePage: async (params: any) => {
    return await request.get({ url: '/bpm/approval-scheme/page', params })
  },

  // 查询审批方案详情
  getScheme: async (id: number) => {
    return await request.get({ url: '/bpm/approval-scheme/get?id=' + id })
  },

  // 创建审批方案草稿
  createDraft: async (data: BpmApprovalSchemeSaveReqVO) => {
    return await request.post({ url: '/bpm/approval-scheme/create-draft', data })
  },

  // 更新审批方案草稿
  updateDraft: async (data: BpmApprovalSchemeSaveReqVO) => {
    return await request.put({ url: '/bpm/approval-scheme/update-draft', data })
  },

  // 提交审批方案待发布
  submit: async (versionId: number) => {
    return await request.put({ url: '/bpm/approval-scheme/submit', data: { versionId } })
  },

  // 发布审批方案
  publish: async (versionId: number) => {
    return await request.put({ url: '/bpm/approval-scheme/publish', data: { versionId } })
  },

  // 停用审批方案
  disable: async (versionId: number) => {
    return await request.put({ url: '/bpm/approval-scheme/disable?versionId=' + versionId })
  },

  // 切换当前生效版本
  switchActiveVersion: async (versionId: number) => {
    return await request.put({ url: '/bpm/approval-scheme/switch-active-version?versionId=' + versionId })
  }
}
