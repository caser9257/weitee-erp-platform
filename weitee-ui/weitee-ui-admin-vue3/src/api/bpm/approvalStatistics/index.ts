import request from '@/config/axios'

export type ApprovalStatisticsSummaryVO = {
  totalCount: number
  processingCount: number
  approvedCount: number
  rejectedCount: number
  cancelledCount: number
}

export type ApprovalStatisticsUserVO = {
  userId: number
  userName?: string | null
  totalCount: number
  processingCount: number
  approvedCount: number
  rejectedCount: number
  cancelledCount: number
}

export const getApprovalStatisticsSummary = async () => {
  return await request.get<ApprovalStatisticsSummaryVO>({ url: '/bpm/approval/statistics/summary' })
}

export const getCurrentApprovalStatisticsSummary = async () => {
  return await request.get<ApprovalStatisticsSummaryVO>({
    url: '/bpm/approval/statistics/current-summary'
  })
}

export const getCurrentApprovalStatisticsUserList = async () => {
  return await request.get<ApprovalStatisticsUserVO[]>({
    url: '/bpm/approval/statistics/current-user-list'
  })
}
