import request from '@/config/axios'

// ERP 工艺路线 VO
export interface ProcessRouteStepVO {
  id?: number
  stepNo: number
  stepCode: string
  stepName: string
  workCenterId?: number
  workCenterName?: string
  outsourceFlag?: boolean
  qcFlag?: boolean
  reportRequired?: boolean
  inspectRequired?: boolean
  prepareTime?: number
  processTime?: number
  moveTime?: number
  waitTime?: number
  batchSize?: number
  sort?: number
  remark?: string
}

export interface ProcessRouteVO {
  id?: number
  routeCode: string
  routeName: string
  productId: number
  productName?: string
  version?: string
  defaultFlag?: boolean
  status: number
  effectiveDate?: string
  expireDate?: string
  remark?: string
  createTime?: string
  steps: ProcessRouteStepVO[]
}

export interface ProcessRoutePageReqVO {
  pageNo: number
  pageSize: number
  routeCode?: string
  routeName?: string
  productId?: number
  status?: number
}

export interface ProcessRouteSaveReqVO {
  id?: number
  routeCode: string
  routeName: string
  productId: number
  version: string
  defaultFlag?: boolean
  effectiveDate?: string
  expireDate?: string
  remark?: string
  steps: ProcessRouteStepVO[]
}

export const ProcessRouteApi = {
  // 查询工艺路线分页
  getProcessRoutePage: async (params: ProcessRoutePageReqVO) => {
    return await request.get({ url: '/erp/process-route/page', params })
  },

  // 查询工艺路线详情
  getProcessRoute: async (id: number) => {
    return await request.get<ProcessRouteVO>({ url: `/erp/process-route/get?id=${id}` })
  },

  // 新增工艺路线
  createProcessRoute: async (data: ProcessRouteSaveReqVO) => {
    return await request.post<number>({ url: '/erp/process-route/create', data })
  },

  // 修改工艺路线
  updateProcessRoute: async (data: ProcessRouteSaveReqVO) => {
    return await request.put<boolean>({ url: '/erp/process-route/update', data })
  },

  // 更新工艺路线状态
  updateProcessRouteStatus: async (id: number, status: number) => {
    return await request.put<boolean>({ url: `/erp/process-route/update-status?id=${id}&status=${status}` })
  },

  // 删除工艺路线
  deleteProcessRoute: async (id: number) => {
    return await request.delete<boolean>({ url: `/erp/process-route/delete?id=${id}` })
  }
}
