import request from '@/config/axios'

export interface NoticeVO {
  id: number | undefined
  title: string
  type: number
  content: string
  status: number
  remark: string
  creator: string
  createTime: Date
}

export interface NoticeBatchUpdateReqVO {
  ids: number[]
  fieldKey: string
  mode: string
  value: string
}

export interface NoticeBatchUpdateResultVO {
  successCount: number
  failureCount: number
  updatedIds: number[]
  failedItems: Array<{
    id: number
    message: string
  }>
}

// 查询公告列表
export const getNoticePage = (params: PageParam) => {
  return request.get({ url: '/system/notice/page', params })
}

// 查询公告详情
export const getNotice = (id: number) => {
  return request.get({ url: '/system/notice/get?id=' + id })
}

// 新增公告
export const createNotice = (data: NoticeVO) => {
  return request.post({ url: '/system/notice/create', data })
}

// 修改公告
export const updateNotice = (data: NoticeVO) => {
  return request.put({ url: '/system/notice/update', data })
}

// 批量修改公告
export const batchUpdateNotice = (data: NoticeBatchUpdateReqVO) => {
  return request.put<NoticeBatchUpdateResultVO>({ url: '/system/notice/batch-update', data })
}

// 删除公告
export const deleteNotice = (id: number) => {
  return request.delete({ url: '/system/notice/delete?id=' + id })
}

// 批量删除公告
export const deleteNoticeList = (ids: number[]) => {
  return request.delete({ url: '/system/notice/delete-list', params: { ids: ids.join(',') } })
}

// 推送公告
export const pushNotice = (id: number) => {
  return request.post({ url: '/system/notice/push?id=' + id })
}
