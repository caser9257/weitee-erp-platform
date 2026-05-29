import request from '@/config/axios'

export interface EmployeeVO {
  id: number
  userId: number
  deptId: number
  name: string
  mobile: string
  idCard: string
  joinDate: string
  status: number
  contractType: number
  remark: string
  createTime: string
}

// 查询员工列表
export const getEmployeePage = async (params: any) => {
  return await request.get({ url: `/hr/employee/page`, params })
}

// 查询员工详情
export const getEmployee = async (id: number) => {
  return await request.get({ url: `/hr/employee/get?id=` + id })
}

// 新增员工
export const createEmployee = async (data: EmployeeVO) => {
  return await request.post({ url: `/hr/employee/create`, data })
}

// 修改员工
export const updateEmployee = async (data: EmployeeVO) => {
  return await request.put({ url: `/hr/employee/update`, data })
}

// 删除员工
export const deleteEmployee = async (id: number) => {
  return await request.delete({ url: `/hr/employee/delete?id=` + id })
}

// 导出员工 Excel
export const exportEmployee = async (params: any) => {
  return await request.download({ url: `/hr/employee/export-excel`, params })
}
