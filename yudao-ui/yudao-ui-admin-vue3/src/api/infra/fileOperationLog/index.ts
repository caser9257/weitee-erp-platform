import request from '@/config/axios'

// 文件操作日志 VO 类型
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
  createTime: Date
}

// 文件操作日志分页查询参数
export interface FileOperationLogPageReqVO {
  pageNo?: number
  pageSize?: number
  fileId?: number
  operation?: string
  userId?: number
  result?: number
  createTime?: Date[]
}

// 查询文件操作日志分页
export const getFileOperationLogPage = (params: FileOperationLogPageReqVO) => {
  return request.get({ url: '/infra/file-operation-log/page', params })
}

// 查询文件操作日志列表
export const getFileOperationLogList = (fileId: number) => {
  return request.get({ url: '/infra/file-operation-log/list', params: { fileId } })
}
