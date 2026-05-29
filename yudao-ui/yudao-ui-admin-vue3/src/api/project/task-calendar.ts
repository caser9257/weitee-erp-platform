import request from '@/config/axios'

export interface CalendarTaskReq {
  projectId: number
  startTime: string
  endTime: string
}

export interface CalendarTaskVO {
  id: number
  projectId: number
  name: string
  startAt?: string
  endAt?: string
  completeAt?: string
  priorityLevel?: number
  priorityName?: string
  priorityColor?: string
  color?: string
  owners?: { userId: number; userName: string }[]
}

export const TaskCalendarApi = {
  // 按日期范围查询任务
  getTaskListByDate: async (params: CalendarTaskReq): Promise<CalendarTaskVO[]> => {
    return await request.get({ url: '/project/task/list-by-date', params })
  }
}
