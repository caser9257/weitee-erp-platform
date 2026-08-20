import request from '@/config/axios'

export interface FileAccessStatsVO {
  id: number
  fileId: number
  statsDate: string
  viewCount: number
  downloadCount: number
  uniqueVisitorCount: number
}

export const getFileAccessStats = (fileId: number) => {
  return request.get<FileAccessStatsVO[]>({ url: '/infra/file-access-stats/list', params: { fileId } })
}

export const getTopFiles = (params: { limit?: number; startDate?: string; endDate?: string }) => {
  return request.get<FileAccessStatsVO[]>({ url: '/infra/file-access-stats/top', params })
}