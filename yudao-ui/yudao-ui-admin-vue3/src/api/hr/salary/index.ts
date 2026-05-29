import request from '@/config/axios'

export interface SalaryVO {
  id: number
  employeeId: number
  employeeName: string
  salaryType: number // 1-固定薪资(行政/职能) 2-计件薪资(生产加工)
  month: string
  baseSalary: number
  performanceSalary: number
  pieceRateSalary: number
  deduction: number
  actualTotal: number
  status: number // 0-待发 1-已发
  remark: string
}

export const getSalaryPage = async (params: any) => {
  return await request.get({ url: `/hr/salary/page`, params })
}
export const getSalary = async (id: number) => {
  return await request.get({ url: `/hr/salary/get?id=` + id })
}
export const createSalary = async (data: SalaryVO) => {
  return await request.post({ url: `/hr/salary/create`, data })
}
export const updateSalary = async (data: SalaryVO) => {
  return await request.put({ url: `/hr/salary/update`, data })
}
export const deleteSalary = async (id: number) => {
  return await request.delete({ url: `/hr/salary/delete?id=` + id })
}
