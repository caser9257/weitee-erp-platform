import request from '@/config/axios'

export interface ProjectRoleTaskVO {
  id: number
  projectId: number
  roleCode: string
  taskType: string
  taskStatus: string
  assigneeUserId?: number
  sourceType?: string
  sourceId?: number
  summary?: string
  dueTime?: Date | string
  finishTime?: Date | string
  remark?: string
}

export interface ProjectVO {
  id: number
  no: string
  name: string
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
  customerId: number
  customerName?: string
  status: number
  deliveryDate?: Date | string
  pcConfirmTime?: Date | string
  mcConfirmTime?: Date | string
  pcRemark?: string
  mcRemark?: string
  remark?: string
  createTime?: Date | string
  milestoneProgress?: number
  todoTasks?: ProjectRoleTaskVO[]
}

export interface ProjectPcConfirmReqVO {
  projectId: number
  remark: string
  currentStageCode: string
}

export interface ProjectMcConfirmReqVO {
  projectId: number
  remark: string
}

export const ProjectCenterApi = {
  getProjectPage: async (params: any) => {
    return await request.get({ url: `/erp/project/page`, params })
  },

  getAssignedProjectPage: async (roleCode: string, params: any) => {
    return await request.get({ url: `/erp/project/assigned-page`, params: { roleCode, ...params } })
  },

  getProject: async (id: number) => {
    return await request.get<ProjectVO>({ url: `/erp/project/get?id=${id}` })
  },

  confirmPc: async (data: ProjectPcConfirmReqVO) => {
    return await request.post({ url: `/erp/project/pc-confirm`, data })
  },

  confirmMc: async (data: ProjectMcConfirmReqVO) => {
    return await request.post({ url: `/erp/project/mc-confirm`, data })
  }
}
