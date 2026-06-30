import { ProjectApi } from '@/api/erp/project'
import { ProjectCenterApi, type ProjectVO } from '@/api/erp/project/projectCenter'

export type ProjectContextVO = ProjectVO

export const ProjectContextApi = {
  getProjectPage: async (params: any) => {
    return await ProjectCenterApi.getProjectPage(params)
  },

  getProject: async (id: number) => {
    return await ProjectCenterApi.getProject(id)
  },

  getProjectSimpleList: async () => {
    return await ProjectApi.getProjectSimpleList()
  }
}
