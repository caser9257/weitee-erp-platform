import request from '@/config/axios'

export interface TaskVO {
  id: number
  parentId: number
  projectId: number
  columnId: number
  flowItemId: number
  flowItemName: string
  name: string
  description: string
  color: string
  startAt: string
  endAt: string
  completeAt: string
  archivedAt: string
  visibility: number
  priorityLevel: number
  priorityName: string
  priorityColor: string
  sort: number
  owners: { userId: number; userName: string }[]
  assistants: { userId: number; userName: string }[]
  taskTags: { name: string; color: string }[]
  subTaskCount: number
  subTaskCompleteCount: number
  percent: number
  createTime: string
}

export const TaskApi = {
  createTask: async (data: any) => {
    return await request.post({ url: '/project/task/create', data })
  },

  updateTask: async (data: any) => {
    return await request.put({ url: '/project/task/update', data })
  },

  deleteTask: async (id: number) => {
    return await request.delete({ url: '/project/task/delete', params: { id } })
  },

  getTask: async (id: number) => {
    return await request.get({ url: '/project/task/get', params: { id } })
  },

  getTaskPage: async (params: any) => {
    return await request.get({ url: '/project/task/page', params })
  },

  completeTask: async (id: number, complete: boolean) => {
    return await request.post({ url: '/project/task/complete', params: { id, complete } })
  },

  sortTask: async (columnId: number, taskIds: number[]) => {
    return await request.post({ url: '/project/task/sort', params: { columnId }, data: taskIds })
  },

  archiveTask: async (id: number, archive: boolean) => {
    return await request.post({ url: '/project/task/archive', params: { id, archive } })
  },

  moveTask: async (taskId: number, targetColumnId: number) => {
    return await request.post({ url: '/project/task/move', params: { taskId, targetColumnId } })
  }
}
