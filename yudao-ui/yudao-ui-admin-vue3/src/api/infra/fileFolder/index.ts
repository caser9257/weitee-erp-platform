import request from '@/config/axios'

// 文件夹 VO 类型
export interface FileFolderVO {
  id: number
  name: string
  parentId: number
  path: string
  icon: string
  sort: number
  status: number
  remark: string
  children?: FileFolderVO[]
  createTime: Date
}

// 文件夹保存请求 VO
export interface FileFolderSaveReqVO {
  id?: number
  name: string
  parentId: number
  icon?: string
  sort?: number
  status?: number
  remark?: string
}

// 查询文件夹列表
export const getFileFolderList = () => {
  return request.get({ url: '/infra/file-folder/list' })
}

// 查询文件夹树
export const getFileFolderTree = () => {
  return request.get({ url: '/infra/file-folder/tree' })
}

// 查询文件夹详情
export const getFileFolder = (id: number) => {
  return request.get({ url: '/infra/file-folder/get?id=' + id })
}

// 创建文件夹
export const createFileFolder = (data: FileFolderSaveReqVO) => {
  return request.post({ url: '/infra/file-folder/create', data })
}

// 更新文件夹
export const updateFileFolder = (data: FileFolderSaveReqVO) => {
  return request.put({ url: '/infra/file-folder/update', data })
}

// 删除文件夹
export const deleteFileFolder = (id: number) => {
  return request.delete({ url: '/infra/file-folder/delete?id=' + id })
}
