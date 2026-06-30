import request from '@/config/axios'

export interface ProjectVO {
  id: number
  name: string
  description: string
  ownerUserId: number
  ownerUserName: string
  personal: boolean
  archivedAt: string
  createTime: string
}

export interface ProjectPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  archived?: boolean
}

export const ProjectManageApi = {
  createProject: async (data: any) => {
    return await request.post({ url: '/project/create', data })
  },

  updateProject: async (data: any) => {
    return await request.put({ url: '/project/update', data })
  },

  deleteProject: async (id: number) => {
    return await request.delete({ url: '/project/delete', params: { id } })
  },

  getProject: async (id: number) => {
    return await request.get({ url: '/project/get', params: { id } })
  },

  getProjectPage: async (params: ProjectPageReqVO) => {
    return await request.get({ url: '/project/page', params })
  },

  archiveProject: async (id: number, archive: boolean) => {
    return await request.post({ url: '/project/archive', params: { id, archive } })
  },

  addMember: async (projectId: number, userId: number, owner: boolean = false) => {
    return await request.post({ url: '/project/member/add', params: { projectId, userId, owner } })
  },

  removeMember: async (projectId: number, userId: number) => {
    return await request.post({ url: '/project/member/remove', params: { projectId, userId } })
  },

  getMembers: async (projectId: number) => {
    return await request.get({ url: '/project/member/list', params: { projectId } })
  }
}
