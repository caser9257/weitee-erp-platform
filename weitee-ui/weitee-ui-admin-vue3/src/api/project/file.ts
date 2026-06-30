import request from '@/config/axios'

export const FileApi = {
  deleteFile: async (id: number) => {
    return await request.delete({ url: '/project/file/delete', params: { id } })
  },

  getFile: async (id: number) => {
    return await request.get({ url: '/project/file/get', params: { id } })
  },

  getFileList: async (params: any) => {
    return await request.get({ url: '/project/file/list', params })
  }
}
