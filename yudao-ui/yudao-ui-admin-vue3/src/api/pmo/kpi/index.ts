import request from '@/config/axios'

// 占位接口骨架，后续按 PMO 经营指标看板定稿补字段
export interface PmoKpiOverviewRespVO {
  projectCount?: number
  warningCount?: number
  onTimeDeliveryRate?: number
  appraisalPendingCount?: number
}

export const PmoKpiApi = {
  getOverview: async (params?: any) => {
    return await request.get({ url: '/pmo/kpi/overview', params })
  }
}
