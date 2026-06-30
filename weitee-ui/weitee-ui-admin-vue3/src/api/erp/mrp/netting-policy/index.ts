import request from '@/config/axios'

export interface MrpNettingPolicyLineVO {
  componentCode: string
  componentName?: string
  componentRole?: string
  enableFlag?: boolean
  sequenceNo?: number
}

export interface MrpNettingPolicyVO {
  id?: number
  code: string
  name: string
  version?: number
  enableFlag?: boolean
  defaultFlag?: boolean
  remark?: string
  businessTypes: string[]
  lines: MrpNettingPolicyLineVO[]
  updateTime?: string | number
}

export interface MrpNettingPolicyPageReqVO {
  pageNo: number
  pageSize: number
  code?: string
  name?: string
  businessType?: string
  enableFlag?: boolean
}

export const MrpNettingPolicyApi = {
  getPolicyPage: async (params: MrpNettingPolicyPageReqVO) => {
    return await request.get({ url: '/erp/mrp-netting-policy/page', params })
  },

  getPolicy: async (id: number) => {
    return await request.get<MrpNettingPolicyVO>({ url: `/erp/mrp-netting-policy/get?id=${id}` })
  },

  createPolicy: async (data: MrpNettingPolicyVO) => {
    return await request.post<number>({ url: '/erp/mrp-netting-policy/create', data })
  },

  updatePolicy: async (data: MrpNettingPolicyVO) => {
    return await request.put({ url: '/erp/mrp-netting-policy/update', data })
  },

  deletePolicy: async (id: number) => {
    return await request.delete({ url: `/erp/mrp-netting-policy/delete?id=${id}` })
  }
}
