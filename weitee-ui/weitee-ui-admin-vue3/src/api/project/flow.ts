import request from '@/config/axios'

export const FlowApi = {
  getFlow: async (projectId: number) => {
    return await request.get({ url: '/project/flow/get', params: { projectId } })
  },

  saveFlow: async (data: any) => {
    return await request.post({ url: '/project/flow/save', data })
  }
}
