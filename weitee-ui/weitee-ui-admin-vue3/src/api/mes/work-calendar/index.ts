import request from '@/config/axios'

export interface WorkCalendarPageReqVO {
  pageNo: number
  pageSize: number
  calendarName?: string
  workCenterId?: number
  status?: number
}

export interface WorkCalendarSaveReqVO {
  id?: number
  calendarName: string
  workCenterId?: number
  effectiveDate?: string
  expireDate?: string
  weekMask: string
  dailyHours: number
  remark?: string
}

export interface WorkCalendarVO {
  id: number
  calendarName: string
  workCenterId?: number
  workCenterName?: string
  effectiveDate?: string
  expireDate?: string
  weekMask: string
  dailyHours: number
  status?: number
  remark?: string
  createTime?: string | Date | number
}

export const WorkCalendarApi = {
  getWorkCalendarPage: async (params: WorkCalendarPageReqVO) => {
    return await request.get({ url: '/mes/work-calendar/page', params })
  },
  getWorkCalendar: async (id: number) => {
    return await request.get<WorkCalendarVO>({ url: `/mes/work-calendar/get?id=${id}` })
  },
  createWorkCalendar: async (data: WorkCalendarSaveReqVO) => {
    return await request.post({ url: '/mes/work-calendar/create', data })
  },
  updateWorkCalendar: async (data: WorkCalendarSaveReqVO) => {
    return await request.put({ url: '/mes/work-calendar/update', data })
  },
  deleteWorkCalendar: async (id: number) => {
    return await request.delete({ url: '/mes/work-calendar/delete', params: { id } })
  }
}
