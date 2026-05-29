import request from '@/config/axios'

export interface PostLevelSummaryVO {
  level: string
  postCount: number
  assignedUserCount: number
  vacancyCount: number
}

export interface PostLevelDashboardVO {
  postCount: number
  assignedUserCount: number
  vacancyCount: number
  keyPostCount: number
  partTimeUserCount: number
  noBackupCount: number
  levelSummaries: PostLevelSummaryVO[]
  riskTips: string[]
}

export interface PostLevelTreeNodeVO {
  id: string
  postId?: number
  label: string
  type: 'level' | 'post'
  level?: string
  deptId?: number
  assignedUserCount?: number
  staffQuota?: number
  vacancyCount?: number
  primaryUserName?: string
  previewUserNames?: string[]
  children?: PostLevelTreeNodeVO[]
}

export interface PostOrgTreeNodeVO {
  id: string
  type: 'root' | 'dept' | 'post'
  deptId?: number
  postId?: number
  parentId?: number
  label: string
  fullPath?: string
  sort?: number
  depth?: number
  childDeptCount?: number
  directPostCount?: number
  postCount?: number
  staffQuota?: number
  assignedUserCount?: number
  status?: number
  level?: string
  children?: PostOrgTreeNodeVO[]
}

export interface PostOrgTreeVO {
  deptCount: number
  postCount: number
  staffQuota: number
  assignedUserCount: number
  maxDepth: number
  tree: PostOrgTreeNodeVO[]
}

export interface PostLevelAssignedUserVO {
  userId: number
  username: string
  nickname: string
  mobile: string
  status: number
  primary: boolean
  startDate?: string
  endDate?: string
  remark?: string
}

export interface PostLevelChangeLogVO {
  userId: number
  nickname: string
  action: string
  actionTime?: string
  remark?: string
}

export interface PostLevelDetailVO {
  postId: number
  code: string
  name: string
  level: string
  deptId?: number
  deptName?: string
  status: number
  sort: number
  staffQuota: number
  keyPosition: boolean
  allowPartTime: boolean
  jobDescription?: string
  remark?: string
  assignedUserCount: number
  primaryUserCount: number
  vacancyCount: number
  riskTags: string[]
  assignedUsers: PostLevelAssignedUserVO[]
  changeLogs: PostLevelChangeLogVO[]
}

export interface PostLevelAssignUserItemVO {
  userId: number
  primary?: boolean
  startDate?: string
  endDate?: string
  remark?: string
}

export interface PostLevelAssignUsersReqVO {
  postId: number
  assignments: PostLevelAssignUserItemVO[]
}

export const getDashboard = async (deptId?: number) => {
  return await request.get<PostLevelDashboardVO>({ url: '/system/post-level/dashboard', params: { deptId } })
}

export const getTree = async (deptId?: number) => {
  return await request.get<PostLevelTreeNodeVO[]>({ url: '/system/post-level/tree', params: { deptId } })
}

export const getDetail = async (postId: number) => {
  return await request.get<PostLevelDetailVO>({ url: '/system/post-level/detail', params: { postId } })
}

export const getOrgTree = async (deptId?: number) => {
  return await request.get<PostOrgTreeVO>({ url: '/system/post-level/org-tree', params: { deptId } })
}

export const assignUsers = async (data: PostLevelAssignUsersReqVO) => {
  return await request.post({ url: '/system/post-level/assign-users', data })
}
