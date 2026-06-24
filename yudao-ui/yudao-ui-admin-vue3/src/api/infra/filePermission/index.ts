import request from '@/config/axios'

// 文件权限 VO 类型
export interface FilePermissionVO {
  id: number
  fileId: number
  grantType: string
  grantTargetId: number
  grantTargetName: string
  permissions: string
  expireTime: Date
  grantUserId: number
  grantUserName: string
  remark: string
  createTime: Date
}

// 文件权限授权请求 VO
export interface FilePermissionSaveReqVO {
  fileId: number
  grantType: string
  grantTargetId: number
  grantTargetName?: string
  permissions: string
  expireTime?: Date
  remark?: string
}

// 授权文件权限
export const grantFilePermission = (data: FilePermissionSaveReqVO) => {
  return request.post({ url: '/infra/file-permission/grant', data })
}

// 撤销文件权限
export const revokeFilePermission = (fileId: number, grantType: string, grantTargetId: number) => {
  return request.delete({ url: '/infra/file-permission/revoke', params: { fileId, grantType, grantTargetId } })
}

// 获取文件的权限列表
export const getFilePermissionList = (fileId: number) => {
  return request.get({ url: '/infra/file-permission/list', params: { fileId } })
}

// 检查用户是否有指定权限
export const checkFilePermission = (fileId: number, permission: string) => {
  return request.get({ url: '/infra/file-permission/check', params: { fileId, permission } })
}
