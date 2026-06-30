import request from '@/config/axios'

export const ColumnApi = {
  getColumnList: async (projectId: number) => {
    return await request.get({ url: '/project/column/list', params: { projectId } })
  },

  createColumn: async (data: any) => {
    return await request.post({ url: '/project/column/create', data })
  },

  updateColumn: async (data: any) => {
    return await request.put({ url: '/project/column/update', data })
  },

  deleteColumn: async (id: number) => {
    return await request.delete({ url: '/project/column/delete', params: { id } })
  },

  sortColumns: async (projectId: number, columnIds: number[]) => {
    return await request.post({ url: '/project/column/sort', params: { projectId }, data: columnIds })
  }
}
