import request from '@/config/axios'

export interface FileOperationLogVO {
  id: number
  fileId: number
  fileName: string
  operation: string
  userId: number
  userName: string
  ip: string
  userAgent: string
  description: string
  result: number
  failReason: string
  createTime: string
}

export const getFileOperationLogPage = (params: PageParam & { fileId?: number; operation?: string; result?: number }) => {
  return request.get({ url: '/infra/file-operation-log/page', params })
}