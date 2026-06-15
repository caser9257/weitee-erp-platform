import request from '@/config/axios'

/**
 * 审批统计数据 VO
 */
export interface ApprovalStatisticsVO {
  totalCount: number      // 总审批数
  processingCount: number // 审批中
  approvedCount: number   // 已通过
  rejectedCount: number   // 已拒绝
  cancelledCount: number  // 已撤回
}

/**
 * 用户审批统计数据 VO
 */
export interface UserApprovalStatisticsVO {
  userId: number
  totalCount: number
  processingCount: number
  approvedCount: number
  rejectedCount: number
  cancelledCount: number
}

/**
 * 获取审批统计概览
 */
export const getApprovalStatistics = (): Promise<ApprovalStatisticsVO> => {
  return request.get({ url: '/bpm/approval/statistics/summary' })
}

/**
 * 获取所有用户的审批统计
 */
export const getAllUserApprovalStatistics = (): Promise<UserApprovalStatisticsVO[]> => {
  return request.get({ url: '/bpm/approval/statistics/user-list' })
}
