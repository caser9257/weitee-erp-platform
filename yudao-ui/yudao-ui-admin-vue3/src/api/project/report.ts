import request from '@/config/axios'

export const ReportApi = {
  createReport: async (data: any) => {
    return await request.post({ url: '/project/report/create', data })
  },

  updateReport: async (data: any) => {
    return await request.put({ url: '/project/report/update', data })
  },

  deleteReport: async (id: number) => {
    return await request.delete({ url: '/project/report/delete', params: { id } })
  },

  getReport: async (id: number) => {
    return await request.get({ url: '/project/report/get', params: { id } })
  },

  getReportPage: async (params: any) => {
    return await request.get({ url: '/project/report/page', params })
  },

  getReceiveReportPage: async (params: any) => {
    return await request.get({ url: '/project/report/receive-page', params })
  }
}
