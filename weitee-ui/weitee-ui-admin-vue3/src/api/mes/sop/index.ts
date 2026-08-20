import request from '@/config/axios'

export interface SopDocumentPageReqVO {
  pageNo: number
  pageSize: number
  sopNo?: string
  title?: string
  status?: number
}

export interface SopDocumentSaveReqVO {
  id?: number
  sopNo: string
  title: string
  version?: string
  content?: string
  attachmentUrl?: string
  effectiveDate?: string
  expireDate?: string
  remark?: string
  routeStepIds: number[]
}

export interface SopDocumentVO {
  id: number
  sopNo: string
  title: string
  version?: string
  content?: string
  attachmentUrl?: string
  status?: number
  effectiveDate?: string
  expireDate?: string
  remark?: string
  routeStepIds?: number[]
  createTime?: string | Date | number
}

export interface SopImportRecordVO {
  id: number
  fileName?: string
  fileUrl?: string
  ocrText?: string
  status?: number
  sopId?: number
  errorMsg?: string
  createTime?: string | Date | number
}

export const SopApi = {
  getSopPage: async (params: SopDocumentPageReqVO) => {
    return await request.get({ url: '/mes/sop/page', params })
  },
  getSop: async (id: number) => {
    return await request.get<SopDocumentVO>({ url: `/mes/sop/get?id=${id}` })
  },
  createSop: async (data: SopDocumentSaveReqVO) => {
    return await request.post({ url: '/mes/sop/create', data })
  },
  updateSop: async (data: SopDocumentSaveReqVO) => {
    return await request.put({ url: '/mes/sop/update', data })
  },
  updateStatus: async (id: number, status: number) => {
    return await request.put({ url: '/mes/sop/update-status', params: { id, status } })
  },
  deleteSop: async (id: number) => {
    return await request.delete({ url: '/mes/sop/delete', params: { id } })
  },
  getPublishedSopsByStepId: async (routeStepId: number) => {
    return await request.get<SopDocumentVO[]>({
      url: '/mes/sop/by-step',
      params: { routeStepId }
    })
  },
  ocrImport: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return await request.post<SopImportRecordVO>({
      url: '/mes/sop-import/ocr',
      data: formData,
      headersType: 'multipart/form-data',
      timeout: 180000
    })
  },
  confirmImport: async (data: {
    importRecordId: number
    sopNo: string
    title: string
    content?: string
    version?: string
    remark?: string
  }) => {
    return await request.put({ url: '/mes/sop-import/confirm', data })
  },
  getImportPage: async (params: { pageNo: number; pageSize: number; status?: number }) => {
    return await request.get({ url: '/mes/sop-import/page', params })
  }
}
