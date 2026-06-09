import request from '@/config/axios'

export type ApprovalSceneVO = {
  id: number
  sceneCode: string
  name: string
  moduleCode: string
  bizType: string
  actionCode: string
  activeSchemeId: number | null
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

export type ApprovalSceneSaveReqVO = {
  id?: number
  sceneCode: string
  name: string
  moduleCode: string
  bizType: string
  actionCode: string
  status: number
  remark?: string
}

// 查询审批场景分页
export const getApprovalScenePage = async (params: ApprovalScenePageReqVO) => {
  return await request.get({ url: '/bpm/approval-scene/page', params })
}

// 查询审批场景详情
export const getApprovalScene = async (id: number) => {
  return await request.get({ url: '/bpm/approval-scene/get', params: { id } })
}

// 根据场景编码查询审批场景
export const getApprovalSceneByCode = async (sceneCode: string) => {
  return await request.get({ url: '/bpm/approval-scene/get-by-code', params: { sceneCode } })
}

// 新增审批场景
export const createApprovalScene = async (data: ApprovalSceneSaveReqVO) => {
  return await request.post({ url: '/bpm/approval-scene/create', data })
}

// 修改审批场景
export const updateApprovalScene = async (data: ApprovalSceneSaveReqVO) => {
  return await request.put({ url: '/bpm/approval-scene/update', data })
}

// 删除审批场景
export const deleteApprovalScene = async (id: number) => {
  return await request.delete({ url: '/bpm/approval-scene/delete', params: { id } })
}

// 绑定审批方案到场景
export const bindSchemeToScene = async (id: number, activeSchemeId: number | null) => {
  return await request.put({ url: '/bpm/approval-scene/bind-scheme', params: { id, activeSchemeId } })
}
