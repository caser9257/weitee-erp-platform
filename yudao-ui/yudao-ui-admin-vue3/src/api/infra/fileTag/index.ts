import request from '@/config/axios'

// 文件标签 VO 类型
export interface FileTagVO {
  id: number
  name: string
  color: string
  icon: string
  sort: number
  createTime: Date
}

// 文件标签保存请求 VO
export interface FileTagSaveReqVO {
  id?: number
  name: string
  color?: string
  icon?: string
  sort?: number
}

// 查询文件标签列表
export const getFileTagList = () => {
  return request.get({ url: '/infra/file-tag/list' })
}

// 创建文件标签
export const createFileTag = (data: FileTagSaveReqVO) => {
  return request.post({ url: '/infra/file-tag/create', data })
}

// 更新文件标签
export const updateFileTag = (data: FileTagSaveReqVO) => {
  return request.put({ url: '/infra/file-tag/update', data })
}

// 删除文件标签
export const deleteFileTag = (id: number) => {
  return request.delete({ url: '/infra/file-tag/delete?id=' + id })
}
