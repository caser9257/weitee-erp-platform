import request from '@/config/axios'

export type ApprovalSceneVO = {
  id: number
  sceneCode: string
  name: string
  moduleCode: string
  bizType: string
  actionCode: string
  activeSchemeId: number | null
  ownerUserId: number
  status: number
  remark: string
  createTime: string
}

export type ApprovalScenePageReqVO = {
  name?: string
  moduleCode?: string
  bizType?: string
  status?: number
  pageNo?: number
  pageSize?: number
}

// 查询审批场景分页
export const getApprovalScenePage = async (params: ApprovalScenePageReqVO) => {
  return await request.get({ url: '/bpm/approval-scene/page', params })
}

// 查询审批场景详情
export const getApprovalScene = async (id: number) => {
  return await request.get({ url: '/bpm/approval-scene/get', params: { id } })
}

// 启用审批场景
export const enableApprovalScene = async (id: number) => {
  return await request.put({ url: '/bpm/approval-scene/enable', params: { id } })
}

// 禁用审批场景
export const disableApprovalScene = async (id: number) => {
  return await request.put({ url: '/bpm/approval-scene/disable', params: { id } })
}

// 删除审批场景
export const deleteApprovalScene = async (id: number) => {
  return await request.delete({ url: '/bpm/approval-scene/delete?id=' + id })
}

// 绑定审批方案
export const bindScheme = async (id: number, activeSchemeId?: number | null) => {
  return await request.put({ url: '/bpm/approval-scene/bind-scheme', params: { id, activeSchemeId } })
}
