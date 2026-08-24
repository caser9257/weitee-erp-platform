import request from '@/config/axios'

export type ProcessDefinitionVO = {
  id: string
  version: number
  deploymentTIme: string
  suspensionState: number
  formType?: number
  formCustomCreatePath?: string
}

export type NotificationPublishCheckMode = 'STRICT' | 'WARN'
export type NotificationSceneCode =
  | 'TASK_ASSIGNED'
  | 'TASK_TIMEOUT'
  | 'PROCESS_APPROVE'
  | 'PROCESS_REJECT'
export type NotificationReceiverType = 'ASSIGNEE' | 'START_USER'
export type InternalMessageSourceType = 'DEFAULT' | 'INLINE'
export type DingTalkSourceType = 'RESERVED' | 'TEMPLATE'

export type InternalMessageSetting = {
  enabled: boolean
  sourceType: InternalMessageSourceType
  title: string
  content: string
}

export type DingTalkTemplateSetting = {
  enabled: boolean
  sourceType: DingTalkSourceType
  templateCode: string
}

export type NotificationScenePolicy = {
  sceneCode: NotificationSceneCode
  enabled: boolean
  receiverType: NotificationReceiverType
  internalMessage: InternalMessageSetting
  dingTalk: DingTalkTemplateSetting
}

export type NotificationPolicySetting = {
  enable: boolean
  publishCheckMode: NotificationPublishCheckMode
  scenes: NotificationScenePolicy[]
}

export type ModelVO = {
  id: number
  formName: string
  key: string
  name: string
  description: string
  category: string
  formType: number
  formId: number
  formCustomCreatePath: string
  formCustomViewPath: string
  processDefinition: ProcessDefinitionVO
  status: number
  remark: string
  createTime: string
  hasUnpublishedChanges?: boolean
  bpmnXml: string
  notificationPolicySetting?: NotificationPolicySetting
}

export type BpmModelSaveReqVO = {
  id?: string | number
  key: string
  name: string
  category?: string
  type: number
  formType: number
  formId?: number
  formCustomCreatePath?: string
  formCustomViewPath?: string
  visible?: boolean
  startUserIds?: number[]
  startDeptIds?: number[]
  managerUserIds?: number[]
  allowCancelRunningProcess?: boolean
  allowWithdrawTask?: boolean
  processIdRule?: unknown
  simpleModel?: unknown
  [key: string]: unknown
}

export const getModelList = async (name: string | undefined) => {
  return await request.get({ url: '/bpm/model/list', params: { name } })
}

export const getModel = async (id: string) => {
  return await request.get({ url: '/bpm/model/get?id=' + id })
}

export const updateModel = async (data: BpmModelSaveReqVO) => {
  return await request.put({ url: '/bpm/model/update', data: data })
}

// 批量修改流程分类的排序
export const updateModelSortBatch = async (ids: number[]) => {
  return await request.put({
    url: `/bpm/model/update-sort-batch`,
    params: {
      ids: ids.join(',')
    }
  })
}

export const updateModelBpmn = async (data: ModelVO) => {
  return await request.put({ url: '/bpm/model/update-bpmn', data: data })
}

// 任务状态修改
export const updateModelState = async (id: number, state: number) => {
  const data = {
    id: id,
    state: state
  }
  return await request.put({ url: '/bpm/model/update-state', data: data })
}

export const createModel = async (data: BpmModelSaveReqVO) => {
  return await request.post({ url: '/bpm/model/create', data: data })
}

export const deleteModel = async (id: number) => {
  return await request.delete({ url: '/bpm/model/delete?id=' + id })
}

export const deployModel = async (id: string | number) => {
  return await request.post({ url: '/bpm/model/deploy?id=' + id })
}

export const cleanModel = async (id: number) => {
  return await request.delete({ url: '/bpm/model/clean?id=' + id })
}
