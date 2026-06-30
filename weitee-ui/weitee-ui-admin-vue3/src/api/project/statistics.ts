import request from '@/config/axios'

export interface StatisticsOverviewVO {
  totalTasks: number
  completedTasks: number
  inProgressTasks: number
  overdueTasks: number
  completionRate: number
  totalMembers: number
}

export const ProjectStatisticsApi = {
  // 获取项目统计概览
  getOverview: async (projectId: number): Promise<StatisticsOverviewVO> => {
    return await request.get({ url: '/project/statistics/overview', params: { projectId } })
  }
}
