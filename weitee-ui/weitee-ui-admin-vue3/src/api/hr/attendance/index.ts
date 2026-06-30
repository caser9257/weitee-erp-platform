import request from '@/config/axios'

export interface AttendanceVO {
  id: number
  employeeId: number
  employeeName: string
  recordDate: string
  clockInTime: string
  clockOutTime: string
  status: number
  remark: string
}

export const getAttendancePage = async (params: any) => {
  return await request.get({ url: `/hr/attendance/page`, params })
}
export const getAttendance = async (id: number) => {
  return await request.get({ url: `/hr/attendance/get?id=` + id })
}
export const createAttendance = async (data: AttendanceVO) => {
  return await request.post({ url: `/hr/attendance/create`, data })
}
export const updateAttendance = async (data: AttendanceVO) => {
  return await request.put({ url: `/hr/attendance/update`, data })
}
export const deleteAttendance = async (id: number) => {
  return await request.delete({ url: `/hr/attendance/delete?id=` + id })
}
