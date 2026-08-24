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

// 删除文件
export const deleteFile = (id: number) => {
  return request.delete({ url: '/infra/file/delete?id=' + id })
}

// 批量删除文件
export const deleteFileList = (ids: number[]) => {
  return request.delete({ url: '/infra/file/delete-list', params: { ids: ids.join(',') } })
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

// ========== 文件版本管理 ==========

// 文件版本 Response VO
export interface FileVersionRespVO {
  id: number
  fileId: number
  version: number
  name: string
  url: string
  size: number
  type: string
  description: string
  createTime: string
}

// 查询文件版本列表
export const getFileVersionList = (fileId: number) => {
  return request.get<FileVersionRespVO[]>({ url: '/infra/file-version/list', params: { fileId } })
}

// 获取文件最新版本号
export const getLatestFileVersion = (fileId: number) => {
  return request.get<number>({ url: '/infra/file-version/latest', params: { fileId } })
}

// 回溯文件到指定版本
export const rollbackFileVersion = (data: { fileId: number; version: number }) => {
  return request.post({ url: '/infra/file-version/rollback', data })
}
