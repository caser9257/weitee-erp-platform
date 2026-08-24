import request from '@/config/axios'

export interface OeeSummaryReqVO {
  workCenterId?: number
  startDate?: string
  endDate?: string
}

export interface OeeSummaryVO {
  workCenterId: number
  workCenterName?: string
  statDate: string
  taskCount: number
  planQty: number
  reportedQty: number
  qualifiedQty: number
  planMinutes: number
  actualMinutes: number
  availabilityRate: number
  achievementRate: number
  qualityRate: number
  oee: number
}

export const OeeApi = {
  getOeeSummary: async (params: OeeSummaryReqVO) => {
    return await request.get<OeeSummaryVO[]>({ url: '/mes/oee/summary', params })
  }
}
