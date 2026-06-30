import request from '@/config/axios'

// 占位接口骨架，后续按绩效考核流程接口定稿补充字段
export interface AppraisalPlanVO {
  id?: number
  title: string
  month?: string
  status?: number
  ownerUserId?: number
  remark?: string
  createTime?: Date | string
}

export interface SelfEvalSubmitReqVO {
  planId: number
  content: string
}

export interface LeaderEvalSubmitReqVO {
  planId: number
  targetUserId: number
  score: number
  comment: string
}

export const AppraisalApi = {
  getAppraisalPlanPage: async (params: any) => {
    return await request.get({ url: '/hr/appraisal-plan/page', params })
  },

  getAppraisalPlan: async (id: number) => {
    return await request.get({ url: `/hr/appraisal-plan/get?id=${id}` })
  },

  createAppraisalPlan: async (data: AppraisalPlanVO) => {
    return await request.post({ url: '/hr/appraisal-plan/create', data })
  },

  submitSelfEval: async (data: SelfEvalSubmitReqVO) => {
    return await request.post({ url: '/hr/appraisal-plan/self-eval', data })
  },

  submitLeaderEval: async (data: LeaderEvalSubmitReqVO) => {
    return await request.post({ url: '/hr/appraisal-plan/leader-eval', data })
  }
}
