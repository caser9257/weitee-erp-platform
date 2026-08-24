import request from '@/config/axios'

export interface FileTagVO {
  id: number
  name: string
  color: string
  icon: string
  sort: number
  createTime: Date
}

export const getFileTagList = () => {
  return request.get<FileTagVO[]>({ url: '/infra/file-tag/list' })
}

export const createFileTag = (data: FileTagVO) => {
  return request.post({ url: '/infra/file-tag/create', data })
}

export const updateFileTag = (data: FileTagVO) => {
  return request.put({ url: '/infra/file-tag/update', data })
}

export const deleteFileTag = (id: number) => {
  return request.delete({ url: '/infra/file-tag/delete?id=' + id })
}