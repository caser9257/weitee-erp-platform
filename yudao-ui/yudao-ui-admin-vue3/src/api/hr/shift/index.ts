import request from '@/config/axios'

export interface ShiftVO {
  id: number
  name: string
  startTime: string
  endTime: string
  status: number
  remark: string
}

export const getShiftPage = async (params: any) => {
  return await request.get({ url: `/hr/shift/page`, params })
}
export const getShift = async (id: number) => {
  return await request.get({ url: `/hr/shift/get?id=` + id })
}
export const createShift = async (data: ShiftVO) => {
  return await request.post({ url: `/hr/shift/create`, data })
}
export const updateShift = async (data: ShiftVO) => {
  return await request.put({ url: `/hr/shift/update`, data })
}
export const deleteShift = async (id: number) => {
  return await request.delete({ url: `/hr/shift/delete?id=` + id })
}
