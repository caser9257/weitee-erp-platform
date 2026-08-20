import request from '@/config/axios'

export interface FilePermissionVO {
  id: number
  fileId: number
  grantType: string
  grantTargetId: number
  grantTargetName: string
  permissions: string
  expireTime: string
  grantUserId: number
  grantUserName: string
  remark: string
  createTime: string
}

export const getFilePermissions = (fileId: number) => {
  return request.get<FilePermissionVO[]>({ url: '/infra/file-permission/list', params: { fileId } })
}

export const grantPermission = (data: {
  fileId: number
  grantType: string
  grantTargetId: number
  grantTargetName?: string
  permissions: string
  expireTime?: string
  remark?: string
}) => {
  return request.post({ url: '/infra/file-permission/grant', data })
}

export const revokePermission = (fileId: number, grantType: string, grantTargetId: number) => {
  return request.delete({ url: '/infra/file-permission/revoke', params: { fileId, grantType, grantTargetId } })
}