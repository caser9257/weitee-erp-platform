import request from '@/config/axios'

export interface GanttTaskVO {
  id: number
  parentId?: number
  projectId: number
  name: string
  startAt?: string
  endAt?: string
  completeAt?: string
  progress: number
  priorityColor?: string
  assigneeName?: string
  dependencies?: number[]
}

export const TaskGanttApi = {
  // 获取甘特图数据
  getTaskGanttData: async (projectId: number): Promise<GanttTaskVO[]> => {
    return await request.get({ url: '/project/task/gantt', params: { projectId } })
  }
}
