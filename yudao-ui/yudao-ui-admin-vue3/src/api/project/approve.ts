import request from '@/config/axios'

export const ApproveApi = {
  createApprove: async (data: any) => {
    return await request.post({ url: '/project/approve/create', data })
  },

  approveAction: async (data: any) => {
    return await request.post({ url: '/project/approve/approve', data })
  },

  getApprove: async (id: number) => {
    return await request.get({ url: '/project/approve/get', params: { id } })
  },

  getApprovePage: async (params: any) => {
    return await request.get({ url: '/project/approve/page', params })
  }
}
