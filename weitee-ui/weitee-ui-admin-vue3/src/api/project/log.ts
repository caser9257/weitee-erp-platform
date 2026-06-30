import request from '@/config/axios'
import { PageResult } from '@/types'

export interface LogPageReq {
  projectId?: number
  taskId?: number
  pageNo?: number
  pageSize?: number
}

export interface LogResp {
  id: number
  projectId: number
  taskId?: number
  columnId?: number
  userId: number
  userName?: string
  userAvatar?: string
  detail: string
  record?: any
  createTime: string
}

export const getLogPage = (params: LogPageReq): Promise<PageResult<LogResp>> => {
  return request.get({ url: '/project/log/page', params })
}
