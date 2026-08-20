import request from '@/config/axios'

export interface TaskExecutionTaskReqVO {
  taskNo: string
}

export interface TaskExecutionReportReqVO {
  taskNo: string
  reportedQty: number
  qualifiedQty: number
  scrapQty: number
  workHour?: number
  batchNo?: string
  remark?: string
}

export interface TaskExecutionVO {
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
  dispatchId?: number
  deviceId?: number
  teamId?: number
  workerUserId?: number
  dispatchStatus?: number
  erpStepStatus?: number
  qcFlag?: boolean
  reportedQty?: number
  qualifiedQty?: number
  scrapQty?: number
}

export const MES_STEP_STATUS = {
  WAIT: 0,
  PROCESSING: 1,
  FINISHED: 2,
  PAUSED: 3
} as const

export const TaskExecutionApi = {
  getTask: async (taskNo: string) => {
    return await request.get<TaskExecutionVO>({
      url: '/mes/task-execution/task',
      params: { taskNo }
    })
  },
  start: async (taskNo: string) => {
    return await request.put<boolean>({ url: '/mes/task-execution/start', params: { taskNo } })
  },
  pause: async (taskNo: string) => {
    return await request.put<boolean>({ url: '/mes/task-execution/pause', params: { taskNo } })
  },
  resume: async (taskNo: string) => {
    return await request.put<boolean>({ url: '/mes/task-execution/resume', params: { taskNo } })
  },
  report: async (data: TaskExecutionReportReqVO) => {
    return await request.post<number>({ url: '/mes/task-execution/report', data })
  },
  finish: async (taskNo: string) => {
    return await request.put<boolean>({ url: '/mes/task-execution/finish', params: { taskNo } })
  }
}
