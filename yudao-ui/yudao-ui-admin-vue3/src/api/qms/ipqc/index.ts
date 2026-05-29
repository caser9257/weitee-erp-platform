import request from '@/config/axios'

// 占位接口骨架，后续按过程检验接口定稿补充字段
export interface IpqcInspectionVO {
  id?: number
  workOrderId?: number
  processCode?: string
  inspectionResult?: number
  inspectorUserId?: number
  remark?: string
  createTime?: Date | string
}

export const IpqcApi = {
  getIpqcPage: async (params: any) => {
    return await request.get({ url: '/qms/ipqc/page', params })
  },

  submitIpqc: async (data: IpqcInspectionVO) => {
    return await request.post({ url: '/qms/ipqc/submit', data })
  }
}
