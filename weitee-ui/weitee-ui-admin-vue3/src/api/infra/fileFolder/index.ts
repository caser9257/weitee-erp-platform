import request from '@/config/axios'

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

export const getFileFolderTree = () => {
  return request.get<FileFolderVO[]>({ url: '/infra/file-folder/tree' })
}

export const getFileFolderList = () => {
  return request.get<FileFolderVO[]>({ url: '/infra/file-folder/list' })
}

export const getFileFolder = (id: number) => {
  return request.get<FileFolderVO>({ url: '/infra/file-folder/get?id=' + id })
}

export const createFileFolder = (data: FileFolderVO) => {
  return request.post({ url: '/infra/file-folder/create', data })
}

export const updateFileFolder = (data: FileFolderVO) => {
  return request.put({ url: '/infra/file-folder/update', data })
}

export const deleteFileFolder = (id: number) => {
  return request.delete({ url: '/infra/file-folder/delete?id=' + id })
}