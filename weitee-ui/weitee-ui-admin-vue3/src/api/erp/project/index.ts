import request from '@/config/axios'

export interface ProjectVO {
  id: number
  no: string
  name: string
  customerId: number
  customerName?: string
  projectType?: string
  businessType?: string
  sourceType?: string
  sourceProjectId?: number
  saleOrderId?: number
  projectManagerId?: number
  projectManagerName?: string
  planCoordinatorId?: number
  planCoordinatorName?: string
  materialControllerId?: number
  materialControllerName?: string
  ownerDeptId?: number
  currentStageCode?: string
  riskLevel?: string
  pcStatus?: string
  mcStatus?: string
  status: number
  deliveryDate: Date | string
  pcConfirmTime?: Date | string
  mcConfirmTime?: Date | string
  pcRemark?: string
  mcRemark?: string
  remark: string
  createTime?: Date
  todoTasks?: Array<{
    id?: number
    projectId?: number
    roleCode?: string
    taskType?: string
    taskStatus?: string
    assigneeUserId?: number
    sourceType?: string
    sourceId?: number
    summary?: string
    dueTime?: Date | string
    finishTime?: Date | string
    remark?: string
  }>
}

export interface ProjectSimpleVO {
  id: number
  no: string
  name: string
}

export const ProjectApi = {
  getProjectPage: async (params: any) => {
    return await request.get({ url: `/erp/project/page`, params })
  },

  getProject: async (id: number) => {
    return await request.get<ProjectVO>({ url: `/erp/project/get?id=` + id })
  },

  createProject: async (data: ProjectVO) => {
    return await request.post({ url: `/erp/project/create`, data })
  },

  updateProject: async (data: ProjectVO) => {
    return await request.put({ url: `/erp/project/update`, data })
  },

  deleteProject: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/project/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  getProjectSimpleList: async () => {
    return await request.get<ProjectSimpleVO[]>({ url: `/erp/project/simple-list` })
  },

  exportProject: async (params: any) => {
    return await request.download({ url: `/erp/project/export-excel`, params })
  }
}
