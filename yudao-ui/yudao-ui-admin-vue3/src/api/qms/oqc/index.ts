import request from '@/config/axios'

// 占位接口骨架，后续按出货检验接口定稿补充字段
export interface OqcInspectionVO {
  id?: number
  deliveryId?: number
  projectId?: number
  inspectionResult?: number
  certificateNo?: string
  inspectorUserId?: number
  remark?: string
  createTime?: Date | string
}

export const OqcApi = {
  getOqcPage: async (params: any) => {
    return await request.get({ url: '/qms/oqc/page', params })
  },

  submitOqc: async (data: OqcInspectionVO) => {
    return await request.post({ url: '/qms/oqc/submit', data })
  }
}
