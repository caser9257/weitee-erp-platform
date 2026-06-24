import request from '@/config/axios'

// 文件预签名地址 Response VO
export interface FilePresignedUrlRespVO {
  // 文件配置编号
  configId: number
  // 文件上传 URL
  uploadUrl: string
  // 文件 URL
  url: string
  // 文件路径
  path: string
}

// 查询文件列表
export const getFilePage = (params: PageParam) => {
  return request.get({ url: '/infra/file/page', params })
}

// 查询回收站文件列表
export const getRecycleFilePage = (params: PageParam) => {
  return request.get({ url: '/infra/file/recycle-page', params })
}

// 删除文件（移入回收站）
export const deleteFile = (id: number, reason?: string) => {
  return request.delete({ url: '/infra/file/delete', params: { id, reason } })
}

// 批量删除文件（移入回收站）
export const deleteFileList = (ids: number[], reason?: string) => {
  return request.delete({ url: '/infra/file/delete-list', params: { ids: ids.join(','), reason } })
}

// 恢复文件（从回收站）
export const restoreFile = (id: number) => {
  return request.put({ url: '/infra/file/restore', params: { id } })
}

// 批量恢复文件
export const restoreFileList = (ids: number[]) => {
  return request.put({ url: '/infra/file/restore-list', params: { ids: ids.join(',') } })
}

// 彻底删除文件（从回收站永久删除）
export const permanentDeleteFile = (id: number) => {
  return request.delete({ url: '/infra/file/permanent-delete', params: { id } })
}

// 批量彻底删除文件
export const permanentDeleteFileList = (ids: number[]) => {
  return request.delete({ url: '/infra/file/permanent-delete-list', params: { ids: ids.join(',') } })
}

// 清空回收站
export const emptyRecycleBin = () => {
  return request.delete({ url: '/infra/file/empty-recycle-bin' })
}

// 获取文件预签名地址
export const getFilePresignedUrl = (name: string, directory?: string) => {
  return request.get<FilePresignedUrlRespVO>({
    url: '/infra/file/presigned-url',
    params: { name, directory }
  })
}

// 创建文件
export const createFile = (data: any) => {
  return request.post({ url: '/infra/file/create', data })
}

// 上传文件
export const updateFile = (data: any, onUploadProgress?: Function) => {
  return request.upload({ url: '/infra/file/upload', data, onUploadProgress })
}
