import request from '@/config/axios'

// 审批委托 VO
export interface BpmApprovalDelegationVO {
  id?: number
  userId?: number
  delegateUserId?: number
  delegateUserName?: string
  startTime?: string
  endTime?: string
  sceneCode?: string
  reason?: string
  status?: number
  createTime?: string
}

// 审批委托保存请求 VO
export interface BpmApprovalDelegationSaveReqVO {
  id?: number
  userId?: number
  delegateUserId: number
  startTime: string
  endTime: string
  sceneCode?: string
  reason?: string
}

// 审批委托 API
export const ApprovalDelegationApi = {
  // 查询审批委托分页
  getDelegationPage: async (params: any) => {
    return await request.get({ url: '/bpm/approval-delegation/page', params })
  },

  // 查询审批委托详情
  getDelegation: async (id: number) => {
    return await request.get({ url: '/bpm/approval-delegation/get?id=' + id })
  },

  // 创建审批委托
  createDelegation: async (data: BpmApprovalDelegationSaveReqVO) => {
    return await request.post({ url: '/bpm/approval-delegation/create', data })
  },

  // 更新审批委托
  updateDelegation: async (data: BpmApprovalDelegationSaveReqVO) => {
    return await request.put({ url: '/bpm/approval-delegation/update', data })
  },

  // 删除审批委托
  deleteDelegation: async (id: number) => {
    return await request.delete({ url: '/bpm/approval-delegation/delete?id=' + id })
  },

  // 查询我的有效委托
  getMyValidDelegations: async () => {
    return await request.get({ url: '/bpm/approval-delegation/my-valid' })
  }
}
