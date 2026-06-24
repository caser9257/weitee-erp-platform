import request from '@/config/axios'

// 文件访问统计 VO 类型
export interface FileAccessStatsVO {
  id: number
  fileId: number
  statsDate: string
  viewCount: number
  downloadCount: number
  uniqueVisitorCount: number
  createTime: Date
}

// 获取文件的访问统计
export const getFileAccessStats = (fileId: number) => {
  return request.get({ url: '/infra/file-access-stats/list', params: { fileId } })
}

// 获取文件在指定日期的统计
export const getFileAccessStatsByDate = (fileId: number, statsDate: string) => {
  return request.get({ url: '/infra/file-access-stats/get', params: { fileId, statsDate } })
}

// 获取指定日期范围内的统计
export const getAccessStatsByDateRange = (startDate: string, endDate: string) => {
  return request.get({ url: '/infra/file-access-stats/date-range', params: { startDate, endDate } })
}

// 获取访问量最多的文件
export const getTopFiles = (limit: number, startDate?: string, endDate?: string) => {
  return request.get({ url: '/infra/file-access-stats/top', params: { limit, startDate, endDate } })
}
