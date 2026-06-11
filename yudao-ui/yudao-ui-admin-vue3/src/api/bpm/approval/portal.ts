import request from '@/config/axios'

const silentBpmHeaders = {
  silentError: true
}

export type ApprovalTodoVO = {
  id: string
  processInstanceName: string
  startUserName: string
  taskName: string
  createTime: string
  processInstanceId: string
  taskId: string
}

export type ApprovalDoneVO = {
  id: string
  processInstanceName: string
  startUserName: string
  taskName: string
  status: number
  completeTime: string
  processInstanceId: string
}

export type ApprovalMineVO = {
  id: string
  processInstanceName: string
  currentNodeName: string
  currentAssignee: string
  status: number
  startTime: string
  processInstanceId: string
}

export type ApprovalCcVO = {
  id: string
  processInstanceName: string
  startUserName: string
  status: number
  createTime: string
  processInstanceId: string
}

export type ApprovalStatisticsVO = {
  pendingCount: number
  approvedCount: number
  submittedCount: number
  ccCount: number
}

// 查询待办列表
export const getApprovalTodoPage = async (params: any) => {
  return await request.get({ url: '/bpm/task/todo-page', params, headers: silentBpmHeaders })
}

// 查询已办列表
export const getApprovalDonePage = async (params: any) => {
  return await request.get({ url: '/bpm/task/done-page', params, headers: silentBpmHeaders })
}

// 查询我发起的列表
export const getApprovalMyPage = async (params: any) => {
  return await request.get({ url: '/bpm/process-instance/my-page', params, headers: silentBpmHeaders })
}

// 查询抄送我的列表
export const getApprovalCcPage = async (params: any) => {
  return await request.get({ url: '/bpm/process-instance/copy/page', params, headers: silentBpmHeaders })
}

// 查询审批统计数据
export const getApprovalStatistics = async () => {
  return await request.get({ url: '/bpm/approval-runtime/statistics', headers: silentBpmHeaders })
}
