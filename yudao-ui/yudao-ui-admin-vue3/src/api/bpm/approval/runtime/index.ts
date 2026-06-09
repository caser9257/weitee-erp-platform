import request from '@/config/axios'

export type ApprovalSnapshotVO = {
  id: number
  sceneCode: string
  bizId: string
  schemeId: number
  schemeVersionId: number
  ruleId: number
  processInstanceId: string
  processDefinitionKey: string
  contextJson: Record<string, any> | null
  processJson: Record<string, any> | null
  notifyJson: Record<string, any> | null
  status: number
  resultReason: string
  createTime: string
}

// 根据流程实例查询快照
export const getSnapshotByProcessInstanceId = async (processInstanceId: string) => {
  return await request.get({
    url: '/bpm/approval-snapshot/get-by-process-instance-id',
    params: { processInstanceId }
  })
}

// 根据场景编码和业务 ID 查询快照
export const getSnapshotBySceneCodeAndBizId = async (sceneCode: string, bizId: string) => {
  return await request.get({
    url: '/bpm/approval-snapshot/get-by-scene-code-and-biz-id',
    params: { sceneCode, bizId }
  })
}
