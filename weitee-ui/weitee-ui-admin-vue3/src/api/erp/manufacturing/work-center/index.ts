import request from '@/config/axios'

// ERP 工作中心 VO
export interface WorkCenterVO {
  id?: number
  centerCode: string
  centerName: string
  deptId?: number
  deptName?: string
  managerUserId?: number
  managerUserName?: string
  enableDeviceDispatch?: boolean
  status: number
  remark?: string
  createTime?: string
}

export interface WorkCenterPageReqVO {
  pageNo: number
  pageSize: number
  centerCode?: string
  centerName?: string
  status?: number
}

export interface WorkCenterSaveReqVO {
  id?: number
  centerCode: string
  centerName: string
  deptId?: number
  managerUserId?: number
  enableDeviceDispatch?: boolean
  status: number
  remark?: string
}

export interface WorkCenterSimpleVO {
  id: number
  centerCode: string
  centerName: string
}

export const WorkCenterApi = {
  // 查询工作中心分页
  getWorkCenterPage: async (params: WorkCenterPageReqVO) => {
    return await request.get({ url: '/erp/work-center/page', params })
  },

  // 查询工作中心详情
  getWorkCenter: async (id: number) => {
    return await request.get<WorkCenterVO>({ url: `/erp/work-center/get?id=${id}` })
  },

  // 查询启用中的工作中心精简列表
  getWorkCenterSimpleList: async () => {
    return await request.get<WorkCenterSimpleVO[]>({ url: '/erp/work-center/simple-list' })
  },

  // 新增工作中心
  createWorkCenter: async (data: WorkCenterSaveReqVO) => {
    return await request.post<number>({ url: '/erp/work-center/create', data })
  },

  // 修改工作中心
  updateWorkCenter: async (data: WorkCenterSaveReqVO) => {
    return await request.put<boolean>({ url: '/erp/work-center/update', data })
  },

  // 删除工作中心
  deleteWorkCenter: async (id: number) => {
    return await request.delete<boolean>({ url: `/erp/work-center/delete?id=${id}` })
  }
}
