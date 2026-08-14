import request from '@/config/axios'

export interface WorkTaskPageReqVO {
  pageNo: number
  pageSize: number
  productionOrderNo?: string
  productionOrderId?: number
  workCenterId?: number
  status?: number
}

export interface WorkTaskVO {
  id: number
  taskNo: string
  productionOrderId: number
  productionOrderNo?: string
  orderStepId: number
  stepNo?: number
  stepCode?: string
  stepName?: string
  workCenterId?: number
  planQty: number
  priority?: number
  planStartTime?: string | Date | number
  planEndTime?: string | Date | number
  actualStartTime?: string | Date | number
  actualEndTime?: string | Date | number
  status?: number
  remark?: string
  createTime?: string | Date | number
}

export const WORK_TASK_STATUS = {
  WAIT_SCHEDULE: 0,
  SCHEDULED: 1,
  PROCESSING: 2,
  FINISHED: 3,
  CANCELED: 4
} as const

export const WorkTaskApi = {
  getWorkTaskPage: async (params: WorkTaskPageReqVO) => {
    return await request.get({ url: '/mes/work-task/page', params })
  },
  getWorkTaskListByOrderId: async (productionOrderId: number) => {
    return await request.get<WorkTaskVO[]>({
      url: '/mes/work-task/list-by-order',
      params: { productionOrderId }
    })
  },
  updatePlanTime: async (data: {
    id: number
    planStartTime: string
    planEndTime: string
    remark?: string
  }) => {
    return await request.put({ url: '/mes/work-task/update-plan-time', data })
  },
  cancelTask: async (id: number) => {
    return await request.put({ url: '/mes/work-task/cancel', params: { id } })
  },
  updatePriority: async (id: number, priority: number) => {
    return await request.put({ url: '/mes/work-task/update-priority', params: { id, priority } })
  },
  schedule: async (productionOrderId?: number) => {
    return await request.put<number>({
      url: '/mes/work-task/schedule',
      params: productionOrderId ? { productionOrderId } : {}
    })
  },
  clearAndReschedule: async (productionOrderId: number) => {
    return await request.put<number>({
      url: '/mes/work-task/clear-and-reschedule',
      params: { productionOrderId }
    })
  },
  detectConflicts: async (workCenterId: number) => {
    return await request.get<
      { taskIdA: number; taskIdB: number; overlapStart: string; overlapEnd: string }[]
    >({
      url: '/mes/work-task/conflicts',
      params: { workCenterId }
    })
  },
  getGanttList: async (
    workCenterId: number,
    startTime: string,
    endTime: string,
    status?: number
  ) => {
    return await request.get<WorkTaskVO[]>({
      url: '/mes/work-task/gantt',
      params: { workCenterId, startTime, endTime, status }
    })
  },
  regenerateTasks: async (productionOrderId: number) => {
    return await request.put({ url: '/mes/work-task/regenerate', params: { productionOrderId } })
  }
}
