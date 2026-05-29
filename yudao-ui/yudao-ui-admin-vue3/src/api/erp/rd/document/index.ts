import request from '@/config/axios'

// 占位接口骨架，后续按研发文档中心接口定稿补充字段
export interface RdDocumentVO {
  id?: number
  name: string
  category?: string
  secrecyLevel?: string
  version?: string
  fileUrl?: string
  remark?: string
  createTime?: Date | string
}

export const RdDocumentApi = {
  getRdDocumentPage: async (params: any) => {
    return await request.get({ url: '/erp/rd-document/page', params })
  },

  getRdDocument: async (id: number) => {
    return await request.get({ url: `/erp/rd-document/get?id=${id}` })
  },

  createRdDocument: async (data: RdDocumentVO) => {
    return await request.post({ url: '/erp/rd-document/create', data })
  },

  updateRdDocument: async (data: RdDocumentVO) => {
    return await request.put({ url: '/erp/rd-document/update', data })
  },

  deleteRdDocument: async (id: number) => {
    return await request.delete({ url: `/erp/rd-document/delete?id=${id}` })
  }
}
