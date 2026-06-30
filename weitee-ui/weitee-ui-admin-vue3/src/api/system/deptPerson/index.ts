import request from '@/config/axios'

export interface DeptPersonTreeNodeVO {
  id: string
  type: 'root' | 'dept' | 'bucket' | 'user'
  deptId?: number
  userId?: number
  parentId?: number
  label: string
  fullPath?: string
  sort?: number
  depth?: number
  childDeptCount?: number
  directUserCount?: number
  deptCount?: number
  userCount?: number
  status?: number
  username?: string
  nickname?: string
  mobile?: string
  children?: DeptPersonTreeNodeVO[]
  leafDeptCount?: number
  maxDepth?: number
}

export interface DeptPersonTreeVO {
  deptCount: number
  userCount: number
  leafDeptCount: number
  unassignedUserCount: number
  maxDepth: number
  tree: DeptPersonTreeNodeVO[]
}

export const getDeptPersonTree = (deptId?: number) => {
  return request.get<DeptPersonTreeVO>({ url: '/system/dept/person-tree', params: { deptId } })
}
