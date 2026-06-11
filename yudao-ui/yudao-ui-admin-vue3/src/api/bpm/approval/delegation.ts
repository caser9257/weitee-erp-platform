import request from '@/config/axios'

export type ApprovalDelegationVO = {
  id: number
  userId: number
  delegateUserId: number
  startTime: string
  endTime: string
  sceneCode: string | null
  reason: string | null
  status: number
  createTime: string
}

export type ApprovalDelegationPageReqVO = {
  userId?: number
  delegateUserId?: number
  status?: number
  pageNo?: number
  pageSize?: number
}

export type ApprovalDelegationSaveReqVO = {
  id?: number
  userId: number
  delegateUserId: number
  startTime: string
  endTime: string
  sceneCode?: string
  reason?: string
}

// 查询审批委托分页
export const getApprovalDelegationPage = async (params: ApprovalDelegationPageReqVO) => {
  return await request.get({ url: '/bpm/approval-delegation/page', params })
}

// 查询审批委托详情
export const getApprovalDelegation = async (id: number) => {
  return await request.get({ url: '/bpm/approval-delegation/get', params: { id } })
}

// 创建审批委托
export const createApprovalDelegation = async (data: ApprovalDelegationSaveReqVO) => {
  return await request.post({ url: '/bpm/approval-delegation/create', data })
}

// 更新审批委托
export const updateApprovalDelegation = async (data: ApprovalDelegationSaveReqVO) => {
  return await request.put({ url: '/bpm/approval-delegation/update', data })
}

// 删除审批委托
export const deleteApprovalDelegation = async (id: number) => {
  return await request.delete({ url: '/bpm/approval-delegation/delete', params: { id } })
}

// 查询我的有效委托
export const getMyValidDelegations = async () => {
  return await request.get({ url: '/bpm/approval-delegation/my-valid' })
}
