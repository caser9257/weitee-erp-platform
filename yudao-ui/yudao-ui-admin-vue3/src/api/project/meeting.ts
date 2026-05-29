import request from '@/config/axios'

export const MeetingApi = {
  createMeeting: async (data: any) => {
    return await request.post({ url: '/project/meeting/create', data })
  },

  updateMeeting: async (data: any) => {
    return await request.put({ url: '/project/meeting/update', data })
  },

  deleteMeeting: async (id: number) => {
    return await request.delete({ url: '/project/meeting/delete', params: { id } })
  },

  getMeeting: async (id: number) => {
    return await request.get({ url: '/project/meeting/get', params: { id } })
  },

  getMeetingPage: async (params: any) => {
    return await request.get({ url: '/project/meeting/page', params })
  }
}
