import request from '@/config/axios'

export interface ProjectLifecycleTimelineVO {
  id: number
  projectId: number
  stageCode: string
  stageName: string
  happenTime: string | Date
  operatorId?: number
  operatorName?: string
  remark?: string
}

export const ProjectLifecycleApi = {
  // 获取项目生命周期时间线
  getTimeline: async (projectId: number) => {
    return await request.get<ProjectLifecycleTimelineVO[]>({ url: `/erp/project/${projectId}/lifecycle-timeline` })
  },

  // 更新项目生命周期阶段
  updateLifecycleStage: async (projectId: number, newStage: string, reason: string) => {
    return await request.put({ url: `/erp/project/${projectId}/lifecycle`, params: { newStage, reason } })
  },

  // 刷新项目聚合状态
  refreshProjectStatus: async (projectId: number) => {
    return await request.put({ url: `/erp/project/${projectId}/refresh-status` })
  }
}
