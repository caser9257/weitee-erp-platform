import request from '@/config/axios'

// 文件业务关联 VO 类型
export interface FileBizRelVO {
  id: number
  fileId: number
  bizType: string
  bizId: number
  bizNo: string
  remark: string
  createTime: Date
}

// 文件业务关联保存请求 VO
export interface FileBizRelSaveReqVO {
  fileId: number
  bizType: string
  bizId: number
  bizNo?: string
  remark?: string
}

// 查询业务单据关联的文件列表
export const getFileBizRelListByBiz = (bizType: string, bizId: number) => {
  return request.get({ url: '/infra/file-biz-rel/list-by-biz', params: { bizType, bizId } })
}

// 查询文件关联的业务单据列表
export const getFileBizRelListByFile = (fileId: number) => {
  return request.get({ url: '/infra/file-biz-rel/list-by-file', params: { fileId } })
}

// 创建文件业务关联
export const createFileBizRel = (data: FileBizRelSaveReqVO) => {
  return request.post({ url: '/infra/file-biz-rel/create', data })
}

// 删除文件业务关联
export const deleteFileBizRel = (fileId: number, bizType: string, bizId: number) => {
  return request.delete({ url: '/infra/file-biz-rel/delete', params: { fileId, bizType, bizId } })
}
