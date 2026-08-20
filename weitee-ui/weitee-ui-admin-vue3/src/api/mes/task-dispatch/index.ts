import request from '@/config/axios'

export interface TaskDispatchPageReqVO {
  pageNo: number
  pageSize: number
  taskNo?: string
  productionOrderNo?: string
  workCenterId?: number
  taskStatus?: number
  dispatchStatus?: number
}

export interface TaskDispatchVO {
  id?: number
  taskId: number
  taskNo: string
  productionOrderId: number
  productionOrderNo?: string
  orderStepId: number
  stepNo?: number
  stepCode?: string
  stepName?: string
  workCenterId?: number
  planQty?: number
  taskStatus?: number
  planStartTime?: string | Date | number
  planEndTime?: string | Date | number
  deviceId?: number
  teamId?: number
  workerUserId?: number
  dispatchStatus?: number
  activeFlag?: number
  dispatchTime?: string | Date | number
  revokeTime?: string | Date | number
  remark?: string
}

export interface TaskDispatchSaveReqVO {
  taskId: number
  deviceId?: number
  teamId?: number
  workerUserId?: number
  remark?: string
}

export const TASK_DISPATCH_STATUS = {
  ASSIGNED: 1,
  REVOKED: 2
} as const

export const TaskDispatchApi = {
  getPage: async (params: TaskDispatchPageReqVO) => {
    return await request.get({ url: '/mes/task-dispatch/page', params })
  },
  getHistory: async (taskId: number) => {
    return await request.get<TaskDispatchVO[]>({
      url: '/mes/task-dispatch/history',
      params: { taskId }
    })
  },
  assign: async (data: TaskDispatchSaveReqVO) => {
    return await request.post<number>({ url: '/mes/task-dispatch/assign', data })
  },
  reassign: async (data: TaskDispatchSaveReqVO) => {
    return await request.put<number>({ url: '/mes/task-dispatch/reassign', data })
  },
  revoke: async (taskId: number) => {
    return await request.put<boolean>({
      url: '/mes/task-dispatch/revoke',
      params: { taskId }
    })
  }
}
