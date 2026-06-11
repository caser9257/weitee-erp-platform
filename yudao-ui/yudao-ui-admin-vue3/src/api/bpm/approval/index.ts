import request from '@/config/axios'

export type ApprovalDetailVO = {
  status: number
  activityNodes: ActivityNodeVO[]
  todoTask: TodoTaskVO | null
  processDefinition: any
  processInstance: any
}

export type ActivityNodeVO = {
  id: string
  name: string
  nodeType: number
  status: number
  startTime: number | null
  endTime: number | null
  tasks: TaskVO[] | null
  candidateStrategy: number | null
  candidateUsers: any[]
}

export type TaskVO = {
  id: string
  ownerUser: any
  assigneeUser: any
  status: number
  reason: string | null
  signPicUrl: string | null
}

export type TodoTaskVO = {
  id: string
  name: string
  createTime: number
  endTime: number | null
  durationInMillis: number | null
  status: number
  reason: string | null
  ownerUser: any
  assigneeUser: any
  taskDefinitionKey: string
  processInstanceId: string
}

export type ApprovalRecordVO = {
  id: number
  approvalId: string
  taskId: string | null
  action: string
  operatorUserId: number
  comment: string | null
  targetUserId: number | null
  targetNodeId: string | null
  createTime: string
}

export type UrgeRecordVO = {
  id: number
  approvalId: string
  taskId: string | null
  urgeUserId: number
  urgeMessage: string | null
  urgeTime: string
}

// 提交审批
export const submitApproval = async (data: { sceneCode: string; bizId: number }) => {
  return await request.post({ url: '/bpm/approval-runtime/submit', data })
}

// 查询审批详情
export const getApprovalDetail = async (sceneCode: string, bizId: number) => {
  return await request.get({
    url: '/bpm/approval-runtime/get-detail',
    params: { sceneCode, bizId }
  })
}

// 查询审批轨迹
export const getApprovalTrail = async (sceneCode: string, bizId: number) => {
  return await request.get({
    url: '/bpm/approval-runtime/get-trail',
    params: { sceneCode, bizId }
  })
}

// 查询审批记录
export const getApprovalRecords = async (approvalId: string) => {
  return await request.get({
    url: '/bpm/approval-runtime/get-records',
    params: { approvalId }
  })
}

// 撤回审批
export const cancelApproval = async (data: {
  sceneCode: string
  bizId: number
  reason?: string
}) => {
  return await request.post({ url: '/bpm/approval-runtime/cancel', data })
}

// 催办审批
export const urgeApproval = async (data: {
  approvalId: string
  taskId?: string
  message: string
}) => {
  return await request.post({ url: '/bpm/approval-runtime/urge', data })
}

// 查询催办记录
export const getUrgeRecords = async (approvalId: string) => {
  return await request.get({
    url: '/bpm/approval-runtime/get-urge-records',
    params: { approvalId }
  })
}

// 获取可驳回的节点列表
export const getReturnableNodes = async (taskId: string) => {
  return await request.get({
    url: '/bpm/approval-runtime/get-returnable-nodes',
    params: { taskId }
  })
}

// 驳回任意节点
export const returnAnyNode = async (data: {
  taskId: string
  targetNodeId: string
  reason?: string
}) => {
  return await request.post({ url: '/bpm/approval-runtime/return-any-node', data })
}

// 批量审批
export const batchApprove = async (data: {
  taskIds: string[]
  comment?: string
}) => {
  return await request.post({ url: '/bpm/approval-runtime/batch-approve', data })
}
